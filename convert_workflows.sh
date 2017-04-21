#!/bin/bash
#
#     convert_workflows.sh
#
# Execute conversion from json into annis with pepper using the CubhuniJSONImporter.
#
# +--------------------------------------------------------------------------------------------+
# |                                        WARNING                                             |
# | Pepper cannot be executed by a script from inside the UHH remote server. This is a problem |
# | of rights that cannot be changed. Thus, an instance of pepper must be copied into a local  |
# | directory and the variable PEPPER_DIR must contain the path of the pepper instance.        |
# +--------------------------------------------------------------------------------------------+
#
# usage:
#   $ bash convert_worflows.sh -c -oad
#
################################################################################################

#
# constants
#

RED_COLOR=$'\e[1;31m'
END_COLOR=$'\e[0m'

OPTIND=1  # POSIX variable, reset in case getopts has been used previously in the shell.

HELP='\n'\
'usage:\n'\
"\tbash $0 [options]\n"\
'\n'\
'options:\n'\
'\t-o    create annis db for ocred texts\n'\
'\t-a    create annis db for altafsir texts\n'\
'\t-d    create annis db for hadith al-islam texts\n'\
'\t-c    clean resources'\
'\n'

# flag parameters
OCRED_FLAG=0
ALTAFSIR_FLAG=0
HADITH_FLAG=0
CLEAN_FLAG=0

CURRENT_PATH=$(echo $PWD)

############################### IMPORTANT ###########################################
# Ihis path must point to the pepper instance to be used. In the pepper instance,   #
# the variable *pepper.dropin.paths* inside the *conf/pepper.properties* must point #
# to the *CubhuniJSONImporter*, which is included in the same path as this script   #
#####################################################################################
PEPPER_DIR=/home/alicia/Desktop/pepper_instances/pepper_2017.03.01  

OCRED_WORKFLOW="$CURRENT_PATH"/ocred_annotated.pepper
ALTAFSIR_WORKFLOW="$CURRENT_PATH"/altafsir_complete.pepper
HADITH_WORKFLOW="$CURRENT_PATH"/hadith_complete.pepper

DATA_INPATH=/home/alicia/COBHUNI/development/corpus/annotation/data/files/expanded
DATA_OUTPATH=/home/alicia/COBHUNI/development/corpus/visualization/data

OCRED_PATH=annotated/ocred_texts
ALTAFSIR_PATH=complete/altafsir
HADITH_PATH=complete/hadith_alislam

OCRED_META=ocred_texts_meta.json
ALTAFSIR_META=altafsir_meta.json
HADITH_META=hadith_alislam_meta.json

################################
#
# functions
#

parse_arguments()
{
  while getopts ':hoadc' opt
  do
    case "$opt" in
      'h') echo -e "$HELP" ; exit 0 ;;
      'o') OCRED_FLAG=1 ;;
      'a') ALTAFSIR_FLAG=1 ;;
      'd') HADITH_FLAG=1 ;;
      'c') CLEAN_FLAG=1 ;;
      '?') echo -e "\n${RED_COLOR} option -$OPTARG not valid${END_COLOR}\n$HELP" >&2 ; exit 1 ;;
    esac
  done

  shift $((OPTIND-1))
  [ "$1" = "--" ] && shift
}

################################
#
# main
#

parse_arguments $@

if [ $CLEAN_FLAG -eq 1 ]; then
    echo -e "Cleaning and compiling pepper module...\n" 1>&2
    mvn dependency:copy-dependencies && mvn clean install assembly:single
fi

cd "$PEPPER_DIR"

echo -e                                               1>&2
echo -e " +----------------------------------------+" 1>&2
echo -e " |    Starting xml to Annis convertion    |" 1>&2
echo -e " +----------------------------------------+\n" 1>&2

if [ $OCRED_FLAG -eq 1 ]; then

    echo -e "Cleaning ocred texts outpath...\n" 1>&2
    /bin/rm "$DATA_OUTPATH"/"$OCRED_PATH"/*
    cp "$CURRENT_PATH"/"$OCRED_META" "$DATA_INPATH"/"$OCRED_PATH"/"$OCRED_META"

    echo -e "Converting ocred texts to annis database...\n\n" 1>&2
    bash pepperStart.sh "$OCRED_WORKFLOW"

fi

if [ $ALTAFSIR_FLAG -eq 1 ]; then

    echo -e "Cleaning altafsir outpath...\n" 1>&2
    /bin/rm "$DATA_OUTPATH"/"$ALTAFSIR_PATH"/*
    cp "$CURRENT_PATH"/"$ALTAFSIR_META" "$DATA_INPATH"/"$ALTAFSIR_PATH"/"$ALTAFSIR_META"

    echo -e "Converting altafsir to annis database...\n\n" 1>&2
    bash pepperStart.sh "$ALTAFSIR_WORKFLOW"

fi

if [ $HADITH_FLAG -eq 1 ]; then

    echo -e "Cleaning hadith al-islam outpath...\n" 1>&2
    /bin/rm "$DATA_OUTPATH"/"$HADITH_PATH"/*
    cp "$CURRENT_PATH"/"$HADITH_META" "$DATA_INPATH"/"$HADITH_PATH"/"$HADITH_META"

    echo -e "Converting hadith al-islam to annis database...\n\n" 1>&2
    bash pepperStart.sh "$HADITH_WORKFLOW"
    
fi
