package backup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import utilities.StringUtilities;

public class StructPropMatcher_remove {

	final static String ontoFile1 = "./files/ESWC_ATMONTO_AIRM/ATMOntoCoreMerged.owl";
	final static String ontoFile2 = "./files/ESWC_ATMONTO_AIRM/airm-mono.owl";
	final static double threshold = 0.9;

	public static void main( String[] args ) throws OWLOntologyCreationException, AlignmentException, IOException {

		BasicAlignment alignment = new URIAlignment();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(new File(ontoFile1));
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(new File(ontoFile2));
		
		URI onto1URI = URI.create(onto1.getOntologyID().getOntologyIRI().toString());
		URI onto2URI = URI.create(onto2.getOntologyID().getOntologyIRI().toString());

		System.out.println("onto1IRI is " + onto1URI);
		System.out.println("onto2IRI is " + onto2URI);

		//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
		alignment.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );

		Set<OWLClass> onto1Cls = onto1.getClassesInSignature();
		Set<OWLClass> onto2Cls = onto2.getClassesInSignature();

		String s1 = null;
		String s2 = null;
		Set<String> props1 = null;
		Set<String> props2 = null;
		double jaccardSim = 0;

		for (OWLClass c1 : onto1Cls) {
			for (OWLClass c2 : onto2Cls) {
				
				s1 = c1.getIRI().getFragment();
				s2 = c2.getIRI().getFragment();

				props1 = getProperties(onto1, s1.toLowerCase());
				props2 = getProperties(onto2, s2.toLowerCase());

				jaccardSim = jaccardSetSim(props1, props2);

				if (jaccardSim > 0 && jaccardSim <= 1.0) {

					alignment.addAlignCell(c1.getIRI().toURI(), c2.getIRI().toURI(), "=", jaccardSim);

				} 
			}
		}

		System.out.println("The alignment contains " + alignment.nbCells() + " cells");
		for (Cell c : alignment) {
			System.out.println(c.getObject1() + " - " + c.getObject2() + ": " + c.getStrength());
		}
		
		System.out.println("The alignment URIs are: ");
		System.out.println(alignment.getOntology1URI() + " and " + alignment.getOntology2URI());

		//store the alignment
		AlignmentVisitor renderer = null;
		PrintWriter writer = null;

		String alignmentFileName = "./files/ATMONTO2AIRMExperiment/alignments-09/" + StringUtilities.stripOntologyName(ontoFile1.toString()) + 
				"-" + StringUtilities.stripOntologyName(ontoFile2.toString()) + "-StructPropMatcher"+threshold+".rdf";

		File outputAlignment = new File(alignmentFileName);

		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		renderer = new RDFRendererVisitor(writer);

		alignment.normalise();
		alignment.cut(threshold);
		alignment.render(renderer);
		writer.flush();
		writer.close();


	}


	public static double getPropSim(OWLClass o1, OWLClass o2) throws OWLOntologyCreationException {

		//get the objects (entities)
		String s1 = o1.getIRI().getFragment();
		String s2 = o2.getIRI().getFragment();

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(new File(ontoFile1));
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(new File(ontoFile2));

		Set<String> props1 = getProperties(onto1, s1);
		Set<String> props2 = getProperties(onto2, s2);

		double jaccardSim = jaccardSetSim(props1, props2);
		double measure = 0;


		if (jaccardSim > 0 && jaccardSim <= 1.0) {
			measure = jaccardSim;
		} else {
			measure = 0;
		}
		return measure;

	}

	/**
	 * Retrieves all properties where an OWLClass is the domain. This includes both object and data properties.
	 * @param onto
	 * @param clsString
	 * @return
	 */
	private static Set<String> getProperties(OWLOntology onto, String clsString) {

		Set<OWLClass> allClasses = onto.getClassesInSignature();		

		Set<String> ops = new HashSet<String>();
		Set<String> dps = new HashSet<String>();

		for (OWLClass cls : allClasses) {
			if (cls.getIRI().getFragment().toLowerCase().equals(clsString)) {

				for (OWLObjectPropertyDomainAxiom op : onto.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN)) {
					if (op.getDomain().equals(cls)) {
						for (OWLObjectProperty oop : op.getObjectPropertiesInSignature()) {
							ops.add(oop.getIRI().getFragment().substring(oop.getIRI().getFragment().lastIndexOf("-") +1));
						}
					}
				}
				
				for (OWLObjectPropertyRangeAxiom op : onto.getAxioms(AxiomType.OBJECT_PROPERTY_RANGE)) {
					if (op.getRange().equals(cls)) {
						for (OWLObjectProperty oop : op.getObjectPropertiesInSignature()) {
							ops.add(oop.getIRI().getFragment().substring(oop.getIRI().getFragment().lastIndexOf("-") +1));
						}
					}
				}

				for (OWLDataPropertyDomainAxiom dp : onto.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN)) {
					if (dp.getDomain().equals(cls)) {
						for (OWLDataProperty odp : dp.getDataPropertiesInSignature()) {
							dps.add(odp.getIRI().getFragment().substring(odp.getIRI().getFragment().lastIndexOf("-") +1));
						}
					}
				}

			}
		}

		//merge all object and data properties into one set
		Set<String> props = new HashSet<String>();
		props.addAll(ops);
		props.addAll(dps);

		return props;

	}
	
	public static String getPropertyCoreConcept(String text) throws IOException, ClassNotFoundException {
		
		System.out.println("The text is " + text);
		
        MaxentTagger maxentTagger = new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
        String tag = maxentTagger.tagString(text);
        
        String[] eachTag = tag.split("\\s+");

        Multimap<String, String> posMap = LinkedListMultimap.create();
        for(int i = 0; i< eachTag.length; i++) {
            posMap.put(eachTag[i].split("_")[0], eachTag[i].split("_")[1]);
        }
        
        StringBuffer sb = new StringBuffer();
        for (Entry<String, String> e : posMap.entries()) {
        	System.out.println("e.getKey is " + e.getKey() + " and this is a: " + e.getValue());
        	if (e.getValue().equals("VB") || e.getValue().equals("VBD") || e.getValue().equals("VBG") || e.getValue().equals("VBP") || e.getValue().equals("VBZ")) {
        		if (e.getKey().length() > 3) {
        		System.out.println("We have a verb with length more than 4: " + e.getKey());
        		sb.append(e.getKey() + " ");
        		break;
        		}
        	} else if (e.getValue().equals("JJ") || e.getValue().equals("JJR") || e.getValue().equals("JJS")) {
        		System.out.println("We have an adjective");
        		sb.append(e.getKey() + " ");
        	}
        	
        	else if (e.getValue().equals("NN") || e.getValue().equals("NNS") || e.getValue().equals("NNP") || e.getValue().equals("NNPS") || e.getValue().equals(".")) {
        		System.out.println("We have a noun");
        		sb.append(e.getKey() + " ");
        		break;
        	}
        		
        }
        
        return sb.toString();
    }

	public static double jaccardSetSim (Set<String> set1, Set<String> set2) {


		int intersection = 0;

		for (String s1 : set1) {
			for (String s2 : set2) {
				if (s1.equals(s2)) {
					intersection += 1;
				}
			}
		}

		int union = (set1.size() + set2.size()) - intersection;

		double jaccardSetSim = (double) intersection / (double) union;

		return jaccardSetSim;
	}

}
