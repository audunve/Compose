package no.ntnu.idi.compose.wordembedding;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import no.ntnu.idi.compose.preprocessing.Preprocessor;

/**
 * @author audunvennesland
 * 22. mai 2017 
 */
public class Labels {

	//get all class labels
	public static ArrayList<String> getClassLabels (File ontoFile) throws OWLOntologyCreationException {
		ArrayList<String> classLabels = new ArrayList<String>();

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		Set<OWLClass> clsSet = onto.getClassesInSignature();
		String thisClass = null;

		for (OWLClass cl : clsSet) {
			thisClass = cl.getIRI().getFragment();
			System.out.println("The class is " + thisClass);
			classLabels.add(Preprocessor.stringTokenize(thisClass, true));
			System.out.println("Adding " + thisClass + " to the ArrayList");
			System.out.println("The number of class names in classList is " + classLabels.size());
		}

		return classLabels;
	}

	//get all object property labels
	public static ArrayList<String> getObjectPropertyLabels (File ontoFile) throws OWLOntologyCreationException {
		ArrayList<String> objectPropertyLabels = new ArrayList<String>();

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		Set<OWLObjectProperty> oPropSet = onto.getObjectPropertiesInSignature();

		String thisObjectProp = null;

		for (OWLObjectProperty cl : oPropSet) {
			thisObjectProp = cl.getIRI().getFragment();
			System.out.println("The object property is " + thisObjectProp);
			objectPropertyLabels.add(Preprocessor.stringTokenize(thisObjectProp, true));
			System.out.println("Adding " + thisObjectProp + " to the ArrayList");
			System.out.println("The number of object properties in classList is " + objectPropertyLabels.size());
		}

		return objectPropertyLabels;
	}

	//get all data property labels
	public static ArrayList<String> getDataPropertyLabels (File ontoFile) throws OWLOntologyCreationException {
		ArrayList<String> dataPropertyLabels = new ArrayList<String>();

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		Set<OWLDataProperty> dPropSet = onto.getDataPropertiesInSignature();

		String thisDataProp = null;

		for (OWLDataProperty cl : dPropSet) {
			thisDataProp = cl.getIRI().getFragment();
			System.out.println("The data property is " + thisDataProp);
			dataPropertyLabels.add(Preprocessor.stringTokenize(thisDataProp, true));
			System.out.println("Adding " + thisDataProp + " to the ArrayList");
			System.out.println("The number of data properties in classList is " + dataPropertyLabels.size());
		}

		return dataPropertyLabels;
	}

	//get all annotation properties
	public static ArrayList<String> getAnnotationPropertyLabels (File ontoFile) throws OWLOntologyCreationException {
		
		ArrayList<String> annPropertyLabels = new ArrayList<String>();

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		OWLDataFactory df = OWLManager.getOWLDataFactory();
		
		//get classes, object properties, data properties in ontology
		Set<OWLClass> cls = onto.getClassesInSignature();
		Set<OWLObjectProperty> prop = onto.getObjectPropertiesInSignature();
		Set<OWLDataProperty> dProp = onto.getDataPropertiesInSignature();
		
		Set<OWLEntity> entities = new HashSet<OWLEntity>();
		entities.addAll(cls);
		entities.addAll(prop);
		entities.addAll(dProp);

		for (OWLEntity e : entities) {
			String label = null;
			Set<OWLAnnotation> annotations = e.getAnnotations(onto);
			for (OWLAnnotation annotation : annotations) {
				if (annotation.getProperty().getIRI().equals(OWLRDFVocabulary.RDFS_COMMENT.getIRI())) {
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					label = val.getLiteral();
					//annPropertyLabels.add(Preprocessor.stringTokenize(label, true));
					annPropertyLabels.add(label);
					System.out.println("Adding " + label + " to the ArrayList");
				}
			}
		}


		return annPropertyLabels;
	}
	
	public static void stripDuplicatesFromFile(File filename) throws IOException {
	    BufferedReader reader = new BufferedReader(new FileReader(filename));
	    Set<String> lines = new HashSet<String>(10000); // maybe should be bigger
	    String line;
	    while ((line = reader.readLine()) != null) {
	        lines.add(line);
	    }
	    reader.close();
	    BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
	    for (String unique : lines) {
	        writer.write(unique);
	        writer.newLine();
	    }
	    writer.close();
	}
	
	
	public static void append (File file, String data) {

		BufferedWriter bw = null;
		FileWriter fw = null;

		try {

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// true = append file
			fw = new FileWriter(file.getAbsoluteFile(), true);
			bw = new BufferedWriter(fw);

			bw.write("\n");
			bw.write(data);

			System.out.println("Done");

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}
		}

	}
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException {
		
		File ontoFile = new File("./files/wordembedding/301303/303.rdf");
		
		Path out = Paths.get("./files/wordembedding/301303/303-classes.txt");
		ArrayList<String> clsList = getClassLabels(ontoFile);
		
		Files.write(out,clsList,Charset.defaultCharset());

	}
}
