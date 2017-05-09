/* Copyright 2017 Alicia Gonzalez Martinez
 * COBHUNI Project, Universität Hamburg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 ********************************************************************/

package de.uni_hamburg.pepper.cubhuni.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FilenameUtils;
import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.impl.PepperImporterImpl;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperImporter;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.PepperModule;
import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleNotReadyException;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a dummy implementation of a {@link PepperImporter}, which can be used
 * as a template to create your own module from. The current implementation
 * creates a corpus-structure looking like this:
 * 
 * <pre>
 *       c1
 *    /      \
 *   c2      c3
 *  /  \    /  \
 * d1  d2  d3  d4
 * </pre>
 * 
 * For each document d1, d2, d3 and d4 the same document-structure is created.
 * The document-structure contains the following structure and annotations:
 * <ol>
 * <li>primary data</li>
 * <li>tokenization</li>
 * <li>part-of-speech annotation for tokenization</li>
 * <li>information structure annotation via spans</li>
 * <li>anaphoric relation via pointing relation</li>
 * <li>syntactic annotations</li>
 * </ol>
 * This dummy implementation is supposed to give you an impression, of how
 * Pepper works and how you can create your own implementation along that dummy.
 * It further shows some basics of creating a simple Salt model. <br/>
 * <strong>This code contains a lot of TODO's. Please have a look at them and
 * adapt the code for your needs </strong> At least, a list of not used but
 * helpful methods:
 * <ul>
 * <li>the salt model to fill can be accessed via {@link #getSaltProject()}</li>
 * <li>customization properties can be accessed via {@link #getPro	private static String KEY_POS_IN;
	private static String KEY_LEMMA_IN;
	private static String KEY_ROOT_IN;
perties()}
 * </li>
 * <li>a place where resources of this bundle are, can be accessed via
 * {@link #getResources()}</li>
 * </ul>
 * If this is the first time, you are implementing a Pepper module, we strongly
 * recommend, to take a look into the 'Developer's Guide for Pepper modules',
 * you will find on
 * <a href="http://corpus-tools.org/pepper/">http://corpus-tools.org/pepper</a>.
 * 
 * @author Alicia Gonzalez 
 */
@Component(name = "CubhuniJSONImporterComponent", factory = "PepperImporterComponentFactory")
public class CubhuniJSONImporter extends PepperImporterImpl implements PepperImporter {
	
	private static final Logger logger = LoggerFactory.getLogger(CubhuniJSONImporter.class);
	
	private static final Properties props = new Properties();
    private static final Map<String, String> ANNOTATIONS = new HashMap<String, String>();
	
	/**
	 * Load constants
	 * @throws IOException 
	 */
	private static void loadConfig() throws IOException {
		String configfilepath = "/home/alicia/COBHUNI/development/corpus/visualization/processing/pepperModules-CUBHUNIModules/config.properties"; //FIXME
		File configFile = new File(configfilepath);
		FileReader configReader = new FileReader(configFile);
		props.load(configReader);
		
	    ANNOTATIONS.put(props.getProperty("persons"), props.getProperty("key_person"));
	    ANNOTATIONS.put(props.getProperty("metamotives"), props.getProperty("key_metamotive"));
	    ANNOTATIONS.put(props.getProperty("motives"), props.getProperty("key_motive"));
	    ANNOTATIONS.put(props.getProperty("sections"), props.getProperty("key_section"));
	    ANNOTATIONS.put(props.getProperty("pages"), props.getProperty("key_page"));
	}
	

	/**
	 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong> <br/>
	 * A constructor for your module. Set the coordinates, with which your
	 * module shall be registered. The coordinates (modules name, version and
	 * supported formats) are a kind of a fingerprint, which should make your
	 * module unique.
	 */
	public CubhuniJSONImporter() {
		super();
		
		try {
			loadConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		setName("CubhuniJSONImporter");
		
		// TODO change suppliers e-mail address
		setSupplierContact(URI.createURI(PepperConfiguration.EMAIL));
		
		// TODO change suppliers homepage
		setSupplierHomepage(URI.createURI(PepperConfiguration.HOMEPAGE));
		
		//TODO add a description of what your module is supposed to do
		setDesc("This is an importer for the data of the COBHUNI Project");
		
		// TODO change "sample" with format name and 1.0 with format version to support
		addSupportedFormat("json", "1.0", null);
		
		// TODO change the endings in endings of files you want to import, see  also predefined endings beginning with 'ENDING_'
		getDocumentEndings().add("json");		
	}
	

	/**
	 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong> <br/>
	 * This method is called by the pepper framework to import the
	 * corpus-structure for the passed {@link SCorpusGraph} object. In Pepper
	 * each import step gets an own {@link SCorpusGraph} to work on. This graph
	 * has to be filled with {@link SCorpus} and {@link SDocument} objects
	 * representing the corpus-structure of the corpus to be imported. <br/>
	 * In many cases, the corpus-structure can be retrieved from the
	 * file-structure of the source files. Therefore Pepper provides a default
	 * mechanism to map the file-structure to corpus-structure. This default
	 * mechanism can be configured. To adapt the default behavior to your needs,
	 * we recommend, to take a look into the 'Developer's Guide for Pepper
	 * modules', you will find on
	 * <a href="https://u.hu-berlin.de/saltnpepper/">https
	 * ://u.hu-berlin.de/saltnpepper/</a>. <br/>
	 * Just to show the creation of a corpus-structure for our sample purpose,
	 * we here create a simple corpus-structure manually. The simple contains a
	 * root-corpus <i>c1</i> having two sub-corpora <i>c2</i> and <i>c3</i>.
	 * Each sub-corpus contains two documents <i>d1</i> and <i>d2</i> for
	 * <i>d3</i> and <i>d4</i> and <i>c1</i> for <i>c3</i>.
	 * 
	 * <pre>
	 *       c1
	 *    /      \
	 *   c2      c3
	 *  /  \    /  \
	 * d1  d2  d3  d4
	 * </pre>
	 * 
	 * The URIs of the corpora and documents would be:
	 * <ul>
	 * <li>salt:/c1</li>
	 * <li>salt:/c1/c2</li>
	 * <li>salt:/c1/c2/d1</li>
	 * <li>salt:/c1/c2/d2</li>
	 * <li>salt:/c1/c3</li>
	 * <li>salt:/c1/c3/d3</li>
	 * <li>salt:/c1/c3/d4</li>
	 * </ul>
	 * 
	 * @param corpusGraph
	 *            the CorpusGraph object, which has to be filled.
	 */
	//FIXME lo unico que faltaria es crear la estructura correcta del corpus con subcorpus. pero los subcorpus pueden tener metadatos?? merece la pena??
	@Override
	public void importCorpusStructure(SCorpusGraph sCorpusGraph) throws PepperModuleException {
		/**
		 * TODO this implementation is just a showcase, in production you might
		 * want to use the default. If yes, uncomment the following line and
		 * delete the rest of the implementation, or delete the entire method to
		 * trigger the default method.
		 */
		
		super.importCorpusStructure(sCorpusGraph);

		/*
		setCorpusGraph(sCorpusGraph);
		// creates the super-corpus c1, in Salt you can create corpora via a URI
		SCorpus c1 = sCorpusGraph.createCorpus(URI.createURI("salt:/c1")).get(0);
		// creates the sub-corpora c2 and c3, in Salt you can also create
		// corpora adding a corpus to a parent
		SCorpus c2 = sCorpusGraph.createCorpus(c1, "c2");
		SCorpus c3 = sCorpusGraph.createCorpus(c1, "c3");

		// creates the documents d1, d2 as children of c2
		SDocument d1 = sCorpusGraph.createDocument(c2, "d1");
		SDocument d2 = sCorpusGraph.createDocument(c2, "d2");

		// creates the documents d3, d4 as children of c3 via the URI mechanism
		SDocument d3 = sCorpusGraph.createDocument(URI.createURI("salt:/c1/c3/d3"));
		SDocument d4 = sCorpusGraph.createDocument(URI.createURI("salt:/c1/c3/d4"));

		// adds a meta-annotation 'author' to all documents, a meta-annotation
		// has a namespace, a name and a value
		d1.createMetaAnnotation(null, "author", "Bart Simpson");
		d2.createMetaAnnotation(null, "author", "Lisa Simpson");
		d3.createMetaAnnotation(null, "author", "Marge Simpson");
		d4.createMetaAnnotation(null, "author", "Homer Simpson");

		// also corpora can take meta-annotations
		c3.createMetaAnnotation(null, "author", "Maggie Simpson");
		*/
	}

	/**
	 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong> <br/>
	 * This method creates a customized {@link PepperMapper} object and returns
	 * it. You can here do some additional initialisations. Thinks like setting
	 * the {@link Identifier} of the {@link SDocument} or {@link SCorpus} object
	 * and the {@link URI} resource is done by the framework (or more in detail
	 * in method {@link #start()}).<br/>
	 * The parameter <code>Identifier</code>, if a {@link PepperMapper} object
	 * should be created in case of the object to map is either an
	 * {@link SDocument} object or an {@link SCorpus} object of the mapper
	 * should be initialized differently. <br/>
	 * Just to show how the creation of such a mapper works, we here create a
	 * sample mapper of type {@link CubhuniJSONMapper}, which only produces a fixed
	 * document-structure in method {@link CubhuniJSONMapper#mapSDocument()} and
	 * enhances the corpora for further meta-annotations in the method
	 * {@link CubhuniJSONMapper#mapSCorpus()}. <br/>
	 * If your mapper needs to have set variables, this is the place to do it.
	 * 
	 * @param Identifier
	 *            {@link Identifier} of the {@link SCorpus} or {@link SDocument}
	 *            to be processed.
	 * @return {@link PepperMapper} object to do the mapping task for object
	 *         connected to given {@link Identifier}
	 */
	public PepperMapper createPepperMapper(Identifier Identifier) {
		CubhuniJSONMapper mapper = new CubhuniJSONMapper();
		/**
		 * TODO Set the exact resource, which should be processed by the created
		 * mapper object, if the default mechanism of importCorpusStructure()
		 * was used, the resource could be retrieved by
		 * getIdentifier2ResourceTable().get(Identifier), just uncomment this
		 * line
		 */
		mapper.setResourceURI(getIdentifier2ResourceTable().get(Identifier));
		return (mapper);
	}

	/**
	 * This class is a dummy implementation for a mapper, to show how it works.
	 * This sample mapper only produces a fixed document-structure in method
	 * {@link CubhuniJSONMapper#mapSDocument()} and enhances the corpora for further
	 * meta-annotations in the method {@link CubhuniJSONMapper#mapSCorpus()}. <br/>
	 * In production, it might be better to implement the mapper in its own
	 * file, we just did it here for compactness of the code.
	 * 
	 * @author Florian Zipser
	 *
	 */
	public static class CubhuniJSONMapper extends PepperMapperImpl {
		/**
		 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong> <br/>
		 * If you need to make any adaptations to the corpora like adding
		 * further meta-annotation, do it here. When whatever you have done
		 * successful, return the status {@link DOCUMENT_STATUS#COMPLETED}. If
		 * anything went wrong return the status {@link DOCUMENT_STATUS#FAILED}.
		 * <br/>
		 * In our dummy implementation, we just add a creation date to each
		 * corpus.
		 */
		@Override
		public DOCUMENT_STATUS mapSCorpus() {
			
			/* add all corpus metadata */
			String metapath = this.getResourceURI().path() + "/" +
                              FilenameUtils.getBaseName(this.getResourceURI().path()) +
                              CubhuniJSONImporter.props.getProperty("meta_file_suf");
			
			JSONParser parser = new JSONParser();
			JSONObject jsonObj;
			try {
				jsonObj = (JSONObject) parser.parse(new FileReader(metapath));
				getCorpus().createMetaAnnotation(null, "ProjectURI", (String) jsonObj.get("ProjectURI"));
				
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
			
			return (DOCUMENT_STATUS.COMPLETED);
		}

		/**
		 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong> <br/>
		 * This is the place for the real work. Here you have to do anything
		 * necessary, to map a corpus to Salt. These could be things like:
		 * reading a file, mapping the content, closing the file, cleaning up
		 * and so on. <br/>
		 * In our dummy implementation, we do not read a file, for not making
		 * the code too complex. We just show how to create a simple
		 * document-structure in Salt, in following steps:
		 * <ol>
		 * <li>creating primary data</li>
		 * <li>creating tokenization</li>
		 * <li>creating part-of-speech annotation for tokenization</li>
		 * <li>creating information structure annotation via spans</li>
		 * <li>creating anaphoric relation via pointing relation</li>
		 * <li>creating syntactic annotations</li>
		 * </ol>
		 */
		@Override
		public DOCUMENT_STATUS mapSDocument() {
			
			this.getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());
			
			// to get the exact resource, which be processed now, call getResources(), make sure, it was set in createMapper()
			URI resource = getResourceURI();
			
			logger.debug("Importing the file {}.", resource);
			
			/* skip metadata files */
			if (resource.path().endsWith(CubhuniJSONImporter.props.getProperty("meta_file_suf"))) {
				return (DOCUMENT_STATUS.COMPLETED);
			}
			
			System.err.println("\033[0;31m::DEBUG::" + " FILE=" + resource + "\033[0m"); //DEBUG
			
			if (resource.trimFileExtension().toString().endsWith(CubhuniJSONImporter.props.getProperty("original"))) {
				this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("source_type_key"),
						                                      (String) CubhuniJSONImporter.props.getProperty("original"));
			}
			
			if (resource.trimFileExtension().toString().endsWith(CubhuniJSONImporter.props.getProperty("commentary"))) {
				this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("source_type_key"),
                                                              (String) CubhuniJSONImporter.props.getProperty("commentary"));
			}
			
			JSONParser parser = new JSONParser();
	        try {
	            Object obj = parser.parse(new FileReader(resource.path()));
	            JSONObject jsonObject = (JSONObject) obj;
	            
	            /*
	             * add document metadata
                 */
	            
	            JSONObject meta = (JSONObject) jsonObject.get(CubhuniJSONImporter.props.getProperty("meta"));
	            
	            
	            String madhab_name = (String) meta.get((String) CubhuniJSONImporter.props.getProperty("madhab_name"));
	            if (madhab_name != null) {
	            	this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("madhab_name"), madhab_name);
	            }
	            
	            if (meta.containsKey((String) CubhuniJSONImporter.props.getProperty("madhab_id"))) {
	            	long madhab_id = (long) meta.get((String) CubhuniJSONImporter.props.getProperty("madhab_id"));
	            	this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("madhab_id"), madhab_id);
	            }
	            
	            String tafsir_name = (String) meta.get((String) CubhuniJSONImporter.props.getProperty("tafsir_name"));
	            if (tafsir_name != null) {
	            	this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("tafsir_name"), tafsir_name);
	            }
	            
	            if (meta.containsKey((String) CubhuniJSONImporter.props.getProperty("tafsir_id"))) {
	            	long tafsir_id = (long) meta.get((String) CubhuniJSONImporter.props.getProperty("tafsir_id"));
	            	this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("tafsir_id"), tafsir_id);
	            }
	            
	            if (meta.containsKey((String) CubhuniJSONImporter.props.getProperty("sura"))) {
	            	long sura = (long) meta.get((String) CubhuniJSONImporter.props.getProperty("sura"));
	            	this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("sura"), sura);
	            }
	            
	            if (meta.containsKey((String) CubhuniJSONImporter.props.getProperty("aya_ini"))) {
	            	long aya_ini = (long) meta.get((String) CubhuniJSONImporter.props.getProperty("aya_ini"));
	            	this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("aya_ini"), aya_ini);
	            }
	            
	            if (meta.containsKey((String) CubhuniJSONImporter.props.getProperty("aya_end"))) {
	            	long aya_end = (long) meta.get((String) CubhuniJSONImporter.props.getProperty("aya_end"));
	            	this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("aya_end"), aya_end);
	            }
	            
	            if (meta.containsKey((String) CubhuniJSONImporter.props.getProperty("book_id"))) {
	            	long book_id = (long) meta.get((String) CubhuniJSONImporter.props.getProperty("book_id"));
	            	this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("book_id"), book_id);
	            }
	            
	            String book_name = (String) meta.get((String) CubhuniJSONImporter.props.getProperty("book_name"));
	            if (book_name != null) {
	            	this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("book_name"), book_name);
	            }
	            	            
	            if (meta.containsKey((String) CubhuniJSONImporter.props.getProperty("chapter_id"))) {
	            	long chapter_id = (long) meta.get((String) CubhuniJSONImporter.props.getProperty("chapter_id"));
	            	this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("chapter_id"), chapter_id);
	            }
	            
	            String chapter_name = (String) meta.get((String) CubhuniJSONImporter.props.getProperty("chapter_name"));
	            if (chapter_name != null) {
	            	this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("chapter_name"), chapter_name);
	            }
	            
	            if (meta.containsKey((String) CubhuniJSONImporter.props.getProperty("subchapter_id"))) {
	            	if (meta.get((String) CubhuniJSONImporter.props.getProperty("subchapter_id")) != null) {
	            		long subchapter_id = (long) meta.get((String) CubhuniJSONImporter.props.getProperty("subchapter_id"));
	            		this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("subchapter_id"), subchapter_id);
	            	}
	            }
	            
	            String subchapter_name = (String) meta.get((String) CubhuniJSONImporter.props.getProperty("subchapter_name"));
	            if (subchapter_name != null) {
	            	this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("subchapter_name"), subchapter_name);
	            }
	            
	            if (meta.containsKey((String) CubhuniJSONImporter.props.getProperty("section_id"))) {
	            	if (meta.get((String) CubhuniJSONImporter.props.getProperty("section_id")) != null ) {
	            		long section_id = (long) meta.get((String) CubhuniJSONImporter.props.getProperty("section_id"));
	            		this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("section_id"), section_id);
	            	}
	            }
	            
	            String section_name = (String) meta.get((String) CubhuniJSONImporter.props.getProperty("section_name"));
	            if (section_name != null) {
	            	this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("section_name"), section_name);
	            }
	            
	            String pages = (String) meta.get((String) CubhuniJSONImporter.props.getProperty("pages"));
	            if (pages != null) {
	            	this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("pages"), pages);
	            }
	            
	            String title = (String) meta.get((String) CubhuniJSONImporter.props.getProperty("title"));
	            if (title != null) {
	            	this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("title"), title);
	            }
	            
	            String author = (String) meta.get((String) CubhuniJSONImporter.props.getProperty("author"));
	            if (author != null) {
	            	this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("author"), author);
	            }
	            
	            String date = (String) meta.get((String) CubhuniJSONImporter.props.getProperty("date"));
	            if (date != null) {
	            	this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("date"), date);
	            }
	            
	            String url = (String) meta.get((String) CubhuniJSONImporter.props.getProperty("url"));
	            if (url != null) {
	            	this.getDocument().createMetaAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("url"), url);
	            }

	            /*
	             * finish adding metadata
                 */
	            
	            String text = (String) jsonObject.get(CubhuniJSONImporter.props.getProperty("text"));
	            STextualDS primaryText = getDocument().getDocumentGraph().createTextualDS(text);
	            //System.err.println("\033[0;31m::DEBUG::" + " text=" + text + "\033[0m"); //DEBUG

	            JSONArray tokens = (JSONArray) jsonObject.get((String) CubhuniJSONImporter.props.getProperty("tokens"));
	            
	            Iterator<JSONObject> iterTokens = tokens.iterator();
	            List<SToken> alltokens = new ArrayList<SToken>();
	            while(iterTokens.hasNext())
	            {
	            	JSONObject tokenObj = iterTokens.next();
	            	String tok = (String) tokenObj.get((String) CubhuniJSONImporter.props.getProperty("tok_tok"));
	            	String pos = (String) tokenObj.get((String) CubhuniJSONImporter.props.getProperty("tok_pos"));
	            	// String lemma = (String) tokenObj.get((String) CubhuniJSONImporter.props.getProperty("tok_lemma"));  //FIXME
	            	// String root = (String) tokenObj.get((String) CubhuniJSONImporter.props.getProperty("tok_root"));  //FIXME
	            	
	            	Integer ini = new Integer(((Long)tokenObj.get((String) CubhuniJSONImporter.props.getProperty("tok_ini"))).intValue());
	            	Integer end = new Integer(((Long)tokenObj.get((String) CubhuniJSONImporter.props.getProperty("tok_end"))).intValue());
	            	
	            	SToken token = getDocument().getDocumentGraph().createToken(primaryText, ini, end);
	            	token.createAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("tok_norm"), tok);
	            	//token.createAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("tok_pos"), pos);  //FIXME
	            	//createdToken.createAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("tok_lemma"), lemma);  //FIXME
	            	//createdToken.createAnnotation(null, (String) CubhuniJSONImporter.props.getProperty("tok_root"), root);  //FIXME
	            	
	            	alltokens.add(token);
	            	
	            	//System.err.println("\033[0;31m::DEBUG tokens:: tok=" + tok + " pos=" + pos + " ini=" + ini + " end=" + end + "\033[0m"); //DEBUG
	            	addProgress(0.16);
	            }
	            
	            Iterator it = ANNOTATIONS.entrySet().iterator();
	            while (it.hasNext())
	            {	
	                Map.Entry pair = (Map.Entry)it.next();
	                String group = (String) pair.getKey();
	                String tag = (String) pair.getValue();
	                
	            	JSONArray annInstance = (JSONArray) jsonObject.get(group);
	            	
	            	if(annInstance == null || annInstance.isEmpty()) continue;
	            	
	            	Iterator<JSONObject> iterAnnotations = annInstance.iterator();
	            	while(iterAnnotations.hasNext())
	            	{
	            		JSONObject annObj = iterAnnotations.next();
	            		String val = (String) annObj.get((String) CubhuniJSONImporter.props.getProperty("annotation_val"));
	            		Integer ini = new Integer(((Long)annObj.get((String) CubhuniJSONImporter.props.getProperty("annotation_ini"))).intValue());
	            		Integer end = new Integer(((Long)annObj.get((String) CubhuniJSONImporter.props.getProperty("annotation_end"))).intValue());

	            		// create new annotation span instance
	            		List<SToken> annotation_set = new ArrayList<SToken>();
	            		
	            		for(int i=ini; i<=end; i++)
	            		{
	            			try
	            			{
	            				annotation_set.add(alltokens.get(i));
	            			}
	            			catch (IndexOutOfBoundsException e)  //DEBUG
	            			{
	            				System.out.println("\033[0;31m::TRACE ERROR" + e + "\033[0m");  //DEBUG
	            				System.out.println("\033[0;31m::DEBUG tokens:: tokens size=" + alltokens.size() + "\033[0m"); //DEBUG
	            				System.out.println("\033[0;31m::DEBUG group=" + group + "  val=" + val + "  ini=" + ini + "  end=" + end + "\033[0m");  //DEBUG
	            				System.exit(1); //DEBUG
	            			}
	            		}
	            		
	            		SSpan annotation = getDocument().getDocumentGraph().createSpan(annotation_set);
	            		annotation.createAnnotation(null, tag, val);
	    			
	            		//System.err.println("\033[0;31m::DEBUG "+ tag +":: val=" + val + " ini=" + ini + " end=" + end + "\033[0m"); //DEBUG
	            		addProgress(0.16);
	            	} 
	            }

	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (ParseException e) {
	            e.printStackTrace();
	        }

			return (DOCUMENT_STATUS.COMPLETED);
			
		}  
	}

	/**
	 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong> <br/>
	 * This method is called by the pepper framework and returns if a corpus
	 * located at the given {@link URI} is importable by this importer. If yes,
	 * 1 must be returned, if no 0 must be returned. If it is not quite sure, if
	 * the given corpus is importable by this importer any value between 0 and 1
	 * can be returned. If this method is not overridden, null is returned.
	 * 
	 * @return 1 if corpus is importable, 0 if corpus is not importable, 0 < X <
	 *         1, if no definitive answer is possible, null if method is not
	 *         overridden
	 */
	public Double isImportable(URI corpusPath) {
		// TODO some code to analyze the given corpus-structure
		//return (null);
		return (1.0); //DEBUG
	}

	// =================================================== optional
	// ===================================================
	/**
	 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong> <br/>
	 * This method is called by the pepper framework after initializing this
	 * object and directly before start processing. Initializing means setting
	 * properties {@link PepperModuleProperties}, setting temporary files,
	 * resources etc. returns false or throws an exception in case of
	 * {@link PepperModule} instance is not ready for any reason. <br/>
	 * So if there is anything to do, before your importer can start working, do
	 * it here.
	 * 
	 * @return false, {@link PepperModule} instance is not ready for any reason,
	 *         true, else.
	 */
	@Override
	public boolean isReadyToStart() throws PepperModuleNotReadyException {
		// TODO make some initializations if necessary
		return (super.isReadyToStart());
	}
}
