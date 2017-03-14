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
import java.net.URISyntaxException;
import java.util.Iterator;

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
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SStructure;
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
	
	
	private static String KEY_TEXT;
	
	private static final Map<String, String> ANNOTATIONS = new HashMap<String, String>();
	
	private static String KEY_TOKENS;

	private static String KEY_TOK;
	private static String KEY_VAL;
		
	private static String KEY_POS_IN;
	private static String KEY_LEMMA_IN;
	private static String KEY_ROOT_IN;
	
	private static String KEY_INI;
	private static String KEY_END;
	
	private static String KEY_NORM;
	private static String KEY_POS_OUT;
	private static String KEY_LEMMA_OUT;
	private static String KEY_ROOT_OUT;
	
	/**
	 * Loads input and output target paths from config file
	 * @throws URISyntaxException 
	 */
	private static void loadConfig() throws IOException {
		
		/*FIXME shit relative path */
		String configfilepath = "/home/alicia/COBHUNI/development/corpus/visualization/complete_corpus/processing/pepperModules-CUBHUNIModules/config.properties";
		File configFile = new File(configfilepath);
		FileReader configReader = new FileReader(configFile);
		Properties props = new Properties();
		props.load(configReader);
		
		KEY_TEXT = props.getProperty("key_text");
		
		ANNOTATIONS.put(props.getProperty("key_persons"), props.getProperty("key_person"));
		ANNOTATIONS.put(props.getProperty("key_motives"), props.getProperty("key_motive"));
		ANNOTATIONS.put(props.getProperty("key_metamotives"), props.getProperty("key_metamotive"));
		
		KEY_TOKENS = props.getProperty("key_tokens");
		
		KEY_TOK = props.getProperty("key_tok");
		KEY_VAL = props.getProperty("key_val");
		
		KEY_POS_IN = props.getProperty("key_pos_in");
		KEY_LEMMA_IN = props.getProperty("key_lemma_in");
		KEY_ROOT_IN = props.getProperty("key_root_in");
		
		KEY_INI = props.getProperty("key_ini");
		KEY_END = props.getProperty("key_end");
		
		KEY_NORM = props.getProperty("key_norm");
		KEY_POS_OUT = props.getProperty("key_pos_out");
		KEY_LEMMA_OUT = props.getProperty("key_lemma_out");
		KEY_ROOT_OUT = props.getProperty("key_root_out");
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
		setDesc("This is a dummy importer and imports a static corpus containing one super-corpus, two sub-corpora and four documents. Each document contains a primary text, a tokenization, part-of-speech annotations,information structure annotations, syntactic annotations and anaphoric relations.");
		
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
			// getScorpus() returns the current corpus object.
			getCorpus().createMetaAnnotation(null, "date", "1989-12-17");

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
			
			// the method getDocument() returns the current document for creating the document-structure
			getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());
			
			// to get the exact resource, which be processed now, call getResources(), make sure, it was set in createMapper()
			URI resource = getResourceURI();
			
			logger.debug("Importing the file {}.", resource);
			
			JSONParser parser = new JSONParser();
	        try {
	            Object obj = parser.parse(new FileReader(resource.path()));
	            JSONObject jsonObject = (JSONObject) obj;

	            String text = (String) jsonObject.get(KEY_TEXT);
	            STextualDS primaryText = getDocument().getDocumentGraph().createTextualDS(text);
	            System.out.println("\033[0;31m::DEBUG::" + " text=" + text + "\033[0m"); //DEBUG

	            JSONArray tokens = (JSONArray) jsonObject.get(KEY_TOKENS);
	            Iterator<JSONObject> iterTokens = tokens.iterator();
	            List<SToken> alltokens = new ArrayList<SToken>();
	            while(iterTokens.hasNext())
	            {
	            	JSONObject tokenObj = iterTokens.next();
	            	String tok = (String) tokenObj.get(KEY_TOK);
	            	String pos = (String) tokenObj.get(KEY_POS_IN);
	            	// String lemma = (String) tokenObj.get(KEY_LEMMA_IN);  //FIXME
	            	// String root = (String) tokenObj.get(KEY_ROOT_IN);  //FIXME
	            	
	            	Integer ini = new Integer(((Long)tokenObj.get(KEY_INI)).intValue());
	            	Integer end = new Integer(((Long)tokenObj.get(KEY_END)).intValue());
	            	
	            	SToken token = getDocument().getDocumentGraph().createToken(primaryText, ini, end);
	            	token.createAnnotation(null, KEY_NORM, tok);
	            	token.createAnnotation(null, KEY_POS_OUT, pos);
	            	//createdToken.createAnnotation(null, KEY_LEMMA_OUT, lemma);  //FIXME
	            	//createdToken.createAnnotation(null, KEY_ROOT_OUT, root);  //FIXME
	            	
	            	alltokens.add(token);
	            	
	            	System.err.println("\033[0;31m::DEBUG tokens:: tok=" + tok + " pos=" + pos + " ini=" + ini + " end=" + end + "\033[0m"); //DEBUG
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
	            		String val = (String) annObj.get(KEY_VAL);
	            		Integer ini = new Integer(((Long)annObj.get(KEY_INI)).intValue());
	            		Integer end = new Integer(((Long)annObj.get(KEY_END)).intValue());

	            		// create new annotation span instance
	            		List<SToken> annotation_set = new ArrayList<SToken>();
	            		for(int i=ini; i<=end; i++){
	            			annotation_set.add(alltokens.get(i));	
	            		}
	            		SSpan annotation = getDocument().getDocumentGraph().createSpan(annotation_set);
	            		annotation.createAnnotation(null, tag, val);
	    			
	            		System.err.println("\033[0;31m::DEBUG "+ tag +":: val=" + val + " ini=" + ini + " end=" + end + "\033[0m"); //DEBUG
	            		addProgress(0.16);
	            	} 
	                //it.remove(); //FIXME borrar?
	            }

	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } catch (ParseException e) {
	            e.printStackTrace();
	        }

			/**
			 * STEP 4: we create some information structure annotations via
			 * spans, spans can be used, to group tokens to a set, which can be
			 * annotated
			 * <table border="1">
			 * <tr>
			 * <td>contrast-focus</td>
			 * <td colspan="9">topic</td>
			 * </tr>
			 * <tr>
			 * <td>Is</td>
			 * <td>this</td>
			 * <td>example</td>
			 * <td>more</td>
			 * <td>complicated</td>
			 * <td>than</td>
			 * <td>it</td>
			 * <td>appears</td>
			 * <td>to</td>
			 * <td>be</td>
			 * </tr>
			 * </table>
			 */
	        
			//SSpan contrastFocus = getDocument().getDocumentGraph().createSpan(tok_is);
			//contrastFocus.createAnnotation(null, "Inf-Struct", "contrast-focus");
			
			/*
			List<SToken> topic_set = new ArrayList<SToken>();
			topic_set.add(tok_thi);
			topic_set.add(tok_exa);
			topic_set.add(tok_mor);
			topic_set.add(tok_com);
			topic_set.add(tok_tha);
			topic_set.add(tok_it);
			topic_set.add(tok_app);
			topic_set.add(tok_to);
			topic_set.add(tok_be);
			
			SSpan topic = getDocument().getDocumentGraph().createSpan(topic_set);
			topic.createAnnotation(null, "Inf-Struct", "topic");

			// we add a progress to notify the user about the process status
			// (this is very helpful, especially for longer taking processes)
			addProgress(0.16);
			*/

			/**
			 * STEP 5: we create anaphoric relation between 'it' and 'this
			 * example', therefore 'this example' must be added to a span. This
			 * makes use of the graph based model of Salt. First we create a
			 * relation, than we set its source and its target node and last we
			 * add the relation to the graph.
			 */
			/*
			List<SToken> target_set = new ArrayList<SToken>();
			target_set.add(tok_thi);
			target_set.add(tok_exa);
			SSpan target = getDocument().getDocumentGraph().createSpan(target_set);
			SPointingRelation anaphoricRel = SaltFactory.createSPointingRelation();
			anaphoricRel.setSource(tok_is);
			anaphoricRel.setTarget(target);
			anaphoricRel.setType("anaphoric");
			// we add the created relation to the graph
			getDocument().getDocumentGraph().addRelation(anaphoricRel);

			// we add a progress to notify the user about the process status
			// (this is very helpful, especially for longer taking processes)
			addProgress(0.16);
			*/

			/**
			 * STEP 6: We create a syntax tree following the Tiger scheme
			 */
			/*
			SStructure root = SaltFactory.createSStructure();
			SStructure sq = SaltFactory.createSStructure();
			SStructure np1 = SaltFactory.createSStructure();
			SStructure adjp1 = SaltFactory.createSStructure();
			SStructure adjp2 = SaltFactory.createSStructure();
			SStructure sbar = SaltFactory.createSStructure();
			SStructure s1 = SaltFactory.createSStructure();
			SStructure np2 = SaltFactory.createSStructure();
			SStructure vp1 = SaltFactory.createSStructure();
			SStructure s2 = SaltFactory.createSStructure();
			SStructure vp2 = SaltFactory.createSStructure();
			SStructure vp3 = SaltFactory.createSStructure();

			// we add annotations to each SStructure node
			root.createAnnotation(null, "cat", "ROOT");
			sq.createAnnotation(null, "cat", "SQ");
			np1.createAnnotation(null, "cat", "NP");
			adjp1.createAnnotation(null, "cat", "ADJP");
			adjp2.createAnnotation(null, "cat", "ADJP");
			sbar.createAnnotation(null, "cat", "SBAR");
			s1.createAnnotation(null, "cat", "S");
			np2.createAnnotation(null, "cat", "NP");
			vp1.createAnnotation(null, "cat", "VP");
			s2.createAnnotation(null, "cat", "S");
			vp2.createAnnotation(null, "cat", "VP");
			vp3.createAnnotation(null, "cat", "VP");

			// we add the root node first
			getDocument().getDocumentGraph().addNode(root);
			SALT_TYPE domRel = SALT_TYPE.SDOMINANCE_RELATION;
			// than we add the rest and connect them to each other
			getDocument().getDocumentGraph().addNode(root, sq, domRel);
			getDocument().getDocumentGraph().addNode(sq, tok_is, domRel); // "Is"
			getDocument().getDocumentGraph().addNode(sq, np1, domRel);
			getDocument().getDocumentGraph().addNode(np1, tok_thi, domRel); // "this"
			getDocument().getDocumentGraph().addNode(np1, tok_exa, domRel); // "example"
			getDocument().getDocumentGraph().addNode(sq, adjp1, domRel);
			getDocument().getDocumentGraph().addNode(adjp1, adjp2, domRel);
			getDocument().getDocumentGraph().addNode(adjp2, tok_mor, domRel); // "more"
			getDocument().getDocumentGraph().addNode(adjp2, tok_com, domRel); // "complicated"
			getDocument().getDocumentGraph().addNode(adjp1, sbar, domRel);
			getDocument().getDocumentGraph().addNode(sbar, tok_tha, domRel); // "than"
			getDocument().getDocumentGraph().addNode(sbar, s1, domRel);
			getDocument().getDocumentGraph().addNode(s1, np2, domRel);
			getDocument().getDocumentGraph().addNode(np2, tok_it, domRel); // "it"
			getDocument().getDocumentGraph().addNode(s1, vp1, domRel);
			getDocument().getDocumentGraph().addNode(vp1, tok_app, domRel); // "appears"
			getDocument().getDocumentGraph().addNode(vp1, s2, domRel);
			getDocument().getDocumentGraph().addNode(s2, vp2, domRel);
			getDocument().getDocumentGraph().addNode(vp2, tok_to, domRel); // "to"
			getDocument().getDocumentGraph().addNode(vp2, vp3, domRel);
			getDocument().getDocumentGraph().addNode(vp3, tok_be, domRel); // "be"
			getDocument().getDocumentGraph().addNode(root, tok_PUN, domRel); // "?"

			// we set progress to 'done' to notify the user about the process
			// status (this is very helpful, especially for longer taking
			// processes)
			setProgress(1.0);
			*/

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
