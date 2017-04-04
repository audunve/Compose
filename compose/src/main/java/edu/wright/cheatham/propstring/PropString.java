package edu.wright.cheatham.propstring;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Evaluator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;


public class PropString extends URIAlignment implements AlignmentProcess {
	
	private String nameA;
	private String nameB;
	private HashMap<OWLEntity, Cell> matched = new HashMap<>();
	
	private static boolean precision = false;
	private static boolean recall = true;
	private static double threshold = 0.8;
	
	private static HashMap<OWLEntity, String> labelMap = new HashMap<>();
	private static HashMap<OWLEntity, String> domainMap = new HashMap<>();
	private static HashMap<OWLEntity, String> rangeMap = new HashMap<>();
	private static HashMap<OWLEntity, ArrayList<String>> instanceMap = new HashMap<>();
	
	
	public PropString() {
		super();
	}
	
	
	public void setNameA(String name) {
		this.nameA = name;
	}
	
	
	public void setNameB(String name) {
		this.nameB = name;
	}

	
	public void init(Object o1, Object o2) throws AlignmentException {
		super.init(o1, o2);
	}
	
	
	@Override
	public void align(Alignment arg0, Properties arg1) throws AlignmentException {

		//get the URIs of the two ontologies
		URI url1 = getOntology1URI();
		URI url2 = getOntology2URI();

		try {
			//get a computed alignment using the align() method
			Alignment alignment = align(url1, url2);
			
			for (Cell cell: alignment) {
				addCell(cell);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public URIAlignment align(URI fileA, URI fileB) throws Exception {

		System.out.println("Loading ontologies");
		init(fileA, fileB);
		
		URIAlignment dummyAlignment = new URIAlignment();
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		IRI iriA = IRI.create(fileA);
		OWLOntology ontA = manager.loadOntologyFromOntologyDocument(iriA);
		
		IRI iriB = IRI.create(fileB);
		OWLOntology ontB = manager.loadOntologyFromOntologyDocument(iriB);
		
		// Train a TFIDF metric for properties
		System.out.println("Training metrics");
		Set<OWLObjectProperty> aObjectProps = ontA.getObjectPropertiesInSignature();
		Set<OWLDataProperty> aDataProps = ontA.getDataPropertiesInSignature();
		Set<OWLAnnotationProperty> aAnnotationProps = ontA.getAnnotationPropertiesInSignature();
		
		Set<OWLEntity> aEntities = new HashSet<>();
		aEntities.addAll(aObjectProps);
		aEntities.addAll(aDataProps);
		aEntities.addAll(aAnnotationProps);
		
		Set<OWLObjectProperty> bObjectProps = ontB.getObjectPropertiesInSignature();
		Set<OWLDataProperty> bDataProps = ontB.getDataPropertiesInSignature();
		Set<OWLAnnotationProperty> bAnnotationProps = ontB.getAnnotationPropertiesInSignature();
		
		Set<OWLEntity> bEntities = new HashSet<>();
		bEntities.addAll(bObjectProps);
		bEntities.addAll(bDataProps);
		bEntities.addAll(bAnnotationProps);
		
		TFIDFMetric propMetric = new TFIDFMetric();
		propMetric.init(aEntities, ontA, bEntities, ontB);
		
		TFIDFMetric generalMetric = new TFIDFMetric();
		generalMetric.init(ontA.getSignature(), ontA, ontB.getSignature(), ontB);

		SoftTFIDFMetric softMetric = new SoftTFIDFMetric(0.9);
		softMetric.init(aEntities, ontA, bEntities, ontB);

		if (precision) {
			System.out.println("Beginning precision-based analysis");
			for (OWLEntity a: aEntities) {

				if (!a.toString().contains(nameA)) continue;
				
				String stringA;
				if (labelMap.containsKey(a)) {
					stringA = labelMap.get(a);
				} else {
					stringA = PartOfSpeech.getCoreConcept(Preprocessing.preprocess(a, ontA));
				}
				
				String exactLabelA = Preprocessing.preprocess(a, ontA);
				
				String domainA = getDomainString(a, ontA);
				String rangeA = getRangeString(a, ontA);

				for (OWLEntity b: bEntities) {

					if (!b.toString().contains(nameB)) continue;
					
					String stringB;
					if (labelMap.containsKey(b)) {
						stringB = labelMap.get(b);
					} else {
						stringB = PartOfSpeech.getCoreConcept(
								Preprocessing.preprocess(b, ontB));
					}
					
					String exactLabelB = Preprocessing.preprocess(b, ontB);
					
					String domainB = getDomainString(b, ontB);
					String rangeB = getRangeString(b, ontB);

					double labelConfA = softMetric.compute(stringA, stringB);
					double labelConfB = softMetric.compute(stringB, stringA);

					double domainConfA = generalMetric.compute(domainA, domainB);
					double domainConfB = generalMetric.compute(domainB, domainA);

					double rangeConfA = generalMetric.compute(rangeA, rangeB);
					double rangeConfB = generalMetric.compute(rangeB, rangeA);

					double labelConfidence = (labelConfA + labelConfB) / 2.0;
					double domainConfidence = (domainConfA + domainConfB) / 2.0;
					double rangeConfidence = (rangeConfA + rangeConfB) / 2.0;
					
					double exactConfA = softMetric.compute(exactLabelA, exactLabelB);
					double exactConfB = softMetric.compute(exactLabelB, exactLabelA);
					double exactConfidence = (exactConfA + exactConfB) / 2.0;

					if (labelConfidence >= threshold && 
							domainConfidence >= threshold && rangeConfidence >= threshold) {
						
						double confidence = (exactConfidence + domainConfidence + 
								rangeConfidence) / 3.0;

						Cell cell = dummyAlignment.addAlignCell(a.getIRI().toURI(), 
								b.getIRI().toURI(), "=", confidence);
						
						boolean replace = true;
						
						if (matched.containsKey(a) && 
								matched.get(a).getStrength() > confidence) {
							replace = false;
						}

						if (matched.containsKey(b) && 
								matched.get(b).getStrength() > confidence) {
							replace = false;
						}

						if (replace) {
							
							// if we're going to replace things, we need to check that
							// whatever a and b matched previously, if anything, is removed
							if (matched.containsKey(a)) {
								String s = matched.get(a).getObject2AsURI().toString();
								OWLEntity e = ontB.getEntitiesInSignature(
										IRI.create(s)).iterator().next();
								matched.remove(e);
							}
							
							if (matched.containsKey(b)) {
								String s = matched.get(b).getObject1AsURI().toString();
								OWLEntity e = ontA.getEntitiesInSignature(
										IRI.create(s)).iterator().next();
								matched.remove(e);
							}
							
//							System.out.println(a + " = " + b + ": " + confidence);
							matched.put(a, cell);
							matched.put(b, cell);	
						}
					}
				}
			}
		}
		
		if (recall) {
			System.out.println("Beginning recall-based analysis");
			for (OWLEntity a: aEntities) {
				
				
				if (!a.toString().contains(nameA)) continue;

				String stringA;
				if (labelMap.containsKey(a)) {
					stringA = labelMap.get(a);
				} else {
					stringA = PartOfSpeech.getCoreConcept(Preprocessing.preprocess(a, ontA));
				}
				
				String exactLabelA = Preprocessing.preprocess(a, ontA);
				
				String domainA = getDomainString(a, ontA);
				String rangeA = getRangeString(a, ontA);

				for (OWLEntity b: bEntities) {

					if (!b.toString().contains(nameB)) continue;

					String stringB;
					if (labelMap.containsKey(b)) {
						stringB = labelMap.get(b);
					} else {
						stringB = PartOfSpeech.getCoreConcept(
								Preprocessing.preprocess(b, ontB));
					}
					
					String exactLabelB = Preprocessing.preprocess(b, ontB);
					
					String domainB = getDomainString(b, ontB);
					String rangeB = getRangeString(b, ontB);

					double labelConfA = softMetric.compute(stringA, stringB);
					double labelConfB = softMetric.compute(stringB, stringA);

					double domainConfA = generalMetric.compute(domainA, domainB);
					double domainConfB = generalMetric.compute(domainB, domainA);

					double rangeConfA = generalMetric.compute(rangeA, rangeB);
					double rangeConfB = generalMetric.compute(rangeB, rangeA);

//					double indConfA = instanceSim(a, ontA, b, ontB);
//					double indConfB = instanceSim(b, ontB, a, ontA);
					
					if (domainA.equals("none") && domainB.equals("none")) {
						domainConfA = 0.0;
						domainConfB = 0.0;
					}
					
					if (rangeA.equals("none") && rangeB.equals("none")) {
						rangeConfA = 0.0;
						rangeConfB = 0.0;
					}

					double labelConfidence = (labelConfA + labelConfB) / 2.0;
					double domainConfidence = (domainConfA + domainConfB) / 2.0;
					double rangeConfidence = (rangeConfA + rangeConfB) / 2.0;
//					double instanceConfidence = (indConfA + indConfB) / 2.0;
					
					double exactConfA = softMetric.compute(exactLabelA, exactLabelB);
					double exactConfB = softMetric.compute(exactLabelB, exactLabelA);
					double exactConfidence = (exactConfA + exactConfB) / 2.0;

					if (labelConfidence >= threshold || //instanceConfidence >= 0.0 || 
							(domainConfidence >= threshold && rangeConfidence >= threshold)) {

//						double confidence = (labelConfidence + domainConfidence + 
//								rangeConfidence + instanceConfidence) / 4.0;
//						
//						if (instanceConfidence < 0) {
						
						double confidence = (exactConfidence + domainConfidence + 
								rangeConfidence) / 3.0;
//						}
						
						Cell cell = dummyAlignment.addAlignCell(a.getIRI().toURI(), 
								b.getIRI().toURI(), "=", confidence);
						
						boolean replace = true;
						
						if (matched.containsKey(a) && 
								matched.get(a).getStrength() > confidence) {
							replace = false;
						}

						if (matched.containsKey(b) && 
								matched.get(b).getStrength() > confidence) {
							replace = false;
						}

						if (replace) {
							
							// if we're going to replace things, we need to check that
							// whatever a and b matched previously, if anything, is removed
							if (matched.containsKey(a)) {
								String s = matched.get(a).getObject2AsURI().toString();
								OWLEntity e = ontB.getEntitiesInSignature(
										IRI.create(s)).iterator().next();
								matched.remove(e);
							}
							
							if (matched.containsKey(b)) {
								String s = matched.get(b).getObject1AsURI().toString();
								
								OWLEntity e = ontA.getEntitiesInSignature(
										IRI.create(s)).iterator().next();
								
								matched.remove(e);
							}
							
							System.out.println(a + " = " + b + ": " + confidence);
							matched.put(a, cell);
							matched.put(b, cell);	
						}
					}
				}
			}
		}

		System.out.println("creating final alignment");
		HashSet<String> done = new HashSet<>();
		URIAlignment alignment = new URIAlignment();
		
		for (Cell cell: matched.values()) {
			
			String key = cell.getObject1AsURI() + "|" + cell.getObject2AsURI();
			
			if (!done.contains(key)) {
				
				alignment.addAlignCell(cell.getObject1AsURI(), cell.getObject2AsURI(), 
						"=", cell.getStrength());
				
				done.add(key);
			}
		}
		return alignment;

	}
	
	
	public static Set<OWLEntity> getDomain(OWLEntity e, OWLOntology ont) {
		
		Set<OWLEntity> neighbors = new HashSet<>();
		
		if (e.isOWLObjectProperty()) {
			Set<OWLClassExpression> temp = e.asOWLObjectProperty().getDomains(ont);
			for (OWLClassExpression t: temp) {
				if (t.isAnonymous()) {
					Set<OWLClass> components = t.getClassesInSignature();
					for (OWLClass component: components) {
						neighbors.add(component);
					}
				} else {
					neighbors.add(t.asOWLClass());
				}
			}
		}
		
		if (e.isOWLDataProperty()) {
			Set<OWLClassExpression> temp = e.asOWLDataProperty().getDomains(ont);
			for (OWLClassExpression t: temp) {
				if (t.isAnonymous()) {
					Set<OWLClass> components = t.getClassesInSignature();
					for (OWLClass component: components) {
						neighbors.add(component);
					}
				} else {
					neighbors.add(t.asOWLClass());
				}
			}
		}
		
		if (e.isOWLAnnotationProperty()) {
			
			Set<OWLAxiom> axioms = ont.getAxioms();
			for (OWLAxiom axiom: axioms) {
				
				String axiomS = axiom.toString();
				if (axiomS.contains(e.toString()) && axiomS.contains("Domain")) {
					
					if (axiomS.contains("<") && axiomS.contains(">")) {
						String domain = axiomS.substring(axiomS.lastIndexOf("<") + 1, 
								axiomS.lastIndexOf(">"));
						neighbors.addAll(ont.getEntitiesInSignature(IRI.create(domain)));
					}
				}
			}
		}
		
		return neighbors;
	}
	
	
	private static Set<OWLEntity> getRange(OWLEntity e, OWLOntology ont) {
		
		Set<OWLEntity> neighbors = new HashSet<>();
		
		if (e.isOWLObjectProperty()) {
			Set<OWLClassExpression> temp = e.asOWLObjectProperty().getRanges(ont);
			for (OWLClassExpression t: temp) {
				if (t.isAnonymous()) {
					Set<OWLClass> components = t.getClassesInSignature();
					for (OWLClass component: components) {
						neighbors.add(component);
					}
				} else {
					neighbors.add(t.asOWLClass());
				}
			}
		}
		
		return neighbors;
	}
	
	
	private static Set<OWLEntity> getNeighborhood(OWLEntity e, OWLOntology ont) {
		
		Set<OWLEntity> neighbors = new HashSet<>();
		neighbors.add(e);
		
		if (e.isOWLObjectProperty()) {
			Set<OWLClassExpression> temp = e.asOWLObjectProperty().getDomains(ont);
			for (OWLClassExpression t: temp) {
				if (t.isAnonymous()) continue;
				neighbors.add(t.asOWLClass());
			}
			
			temp = e.asOWLObjectProperty().getRanges(ont);
			for (OWLClassExpression t: temp) {
				if (t.isAnonymous()) continue;
				neighbors.add(t.asOWLClass());
			}
		}
		
		if (e.isOWLDataProperty()) {
			Set<OWLClassExpression> temp = e.asOWLDataProperty().getDomains(ont);
			for (OWLClassExpression t: temp) {
				if (t.isAnonymous()) continue;
				neighbors.add(t.asOWLClass());
			}
		}
		
		if (e.isOWLAnnotationProperty()) {
			neighbors.addAll(e.asOWLAnnotationProperty().getClassesInSignature());
		}
		
		return neighbors;
	}
	
	
	@SuppressWarnings("unused")
	private static String getNeighborString(OWLEntity e, OWLOntology ont) {
		
		String s = "";
		
		Set<OWLEntity> neighbors = getNeighborhood(e, ont);
		for (OWLEntity neighbor: neighbors) {
			s += " " + Preprocessing.preprocess(neighbor, ont);
		}
		
		return s.trim();
	}
	
	
	public static String getDomainString(OWLEntity e, OWLOntology ont) {
		
		if (domainMap.containsKey(e)) return domainMap.get(e);
		
		String s = "";
		
		Set<OWLEntity> neighbors = getDomain(e, ont);
		for (OWLEntity neighbor: neighbors) {
			s += " " + Preprocessing.preprocess(neighbor, ont);
		}
		
		s = s.trim();
		if (s.length() == 0) {
			s = "none";
		}
		
		domainMap.put(e, s);
		return s;
	}
	
	
	public static String getRangeString(OWLEntity e, OWLOntology ont) {
		
		if (rangeMap.containsKey(e)) return rangeMap.get(e);
		
		String s = "";
		
		Set<OWLEntity> neighbors = getRange(e, ont);
		for (OWLEntity neighbor: neighbors) {
			s += " " + Preprocessing.preprocess(neighbor, ont);
		}
		
		if (e.isOWLDataProperty()) {
			s += " literal";
		}
		
		s = s.trim();
		if (s.length() == 0) {
			s = "none";
		}
		
		rangeMap.put(e,  s);
		return s;
	}
	
	
	public static ArrayList<String> getInstanceData(OWLEntity e, OWLOntology ont) {
		
		if (instanceMap.containsKey(e)) return instanceMap.get(e);
		
		ArrayList<String> data = new ArrayList<>();
		
		// get all of the instance axioms involving each entity
		Set<OWLAxiom> axioms = ont.getReferencingAxioms(e);
		
		for (OWLAxiom axiom: axioms) {
			if (axiom.toString().contains("ObjectPropertyAssertion")) {
				
				OWLObjectPropertyAssertionAxiom it = (OWLObjectPropertyAssertionAxiom) axiom;
				OWLIndividual subject = it.getSubject();
				OWLIndividual object = it.getObject();
				
				String subjectString = "none";
				if (!subject.isAnonymous()) {
					subjectString = Preprocessing.preprocess(
							subject.asOWLNamedIndividual(), ont);
				}
				
				String objectString = "none";
				if (!object.isAnonymous()) {
					objectString = Preprocessing.preprocess(
							object.asOWLNamedIndividual(), ont);
				}
				
				if (!subjectString.equals("none") && !objectString.equals("none"))
					data.add(subjectString + " -> " + objectString);
			}
			
			if (axiom.toString().contains("DataPropertyAssertion")) {
				
				OWLDataPropertyAssertionAxiom it = (OWLDataPropertyAssertionAxiom) axiom;
				OWLIndividual subject = it.getSubject();
				OWLLiteral object = it.getObject();
				
				String subjectString = "none";
				if (!subject.isAnonymous()) {
					subjectString = Preprocessing.preprocess(
							subject.asOWLNamedIndividual(), ont);
				}
				
				String objectString = object.getLiteral();
				
				data.add(subjectString + " " + objectString);
			}
			
			if (axiom.toString().contains("AnnotationAssertion") && 
					!axiom.toString().contains("rdfs:comment")) {
				
				OWLAnnotationAssertionAxiom it = (OWLAnnotationAssertionAxiom) axiom;
				OWLAnnotationSubject subject = it.getSubject();
				OWLAnnotationValue object = it.getValue();

				String subjectString = Preprocessing.preprocess(subject.toString());
				String objectString = object.toString();
				
				data.add(subjectString + " " + objectString);
			}
		}
		
		instanceMap.put(e, data);
		return data;
	}
	
	
	public static void main(String[] args) throws Exception {

		///PropStringV1/OAEI-16-conference/ontologies
		///PropStringV1/src/PropString.java
		
		///compose/src/main/java/no/ntnu/idi/compose/Matchers/TestMatcher.java
		///compose/files/OAEI-16-conference/ontologies
		
		//File f1 = new File("./data/BIBO.owl");

		String name1 = "";
		String name2 = "";

		PropString matcher = new PropString();

		matcher.setNameA(name1);
		matcher.setNameB(name2);
		

		File f1 = new File ("./experiment_eswc17/ontologies/biblio.rdf");
		File f2 = new File ("./experiment_eswc17/ontologies/bibo.owl");
				
				Alignment alignment = matcher.align(f1.toURI(), f2.toURI());

				String alignmentFileName = "./experiment_eswc17/ontologies/PropStringTest.rdf";
				
				File outputAlignment = new File(alignmentFileName);

		FileOutputStream os = new FileOutputStream(outputAlignment);

		PrintWriter writer = new PrintWriter (
				new OutputStreamWriter(os, "UTF-8" ), true);

		AlignmentVisitor renderer = new RDFRendererVisitor(writer);
		alignment.render(renderer);
		writer.close();
		

		System.out.println("Completed matching process!");
	
			
		}
	}
	
	class Match {
		
		public OWLEntity a;
		public OWLEntity b;
		public double confidence;
		
		public Match(OWLEntity a, OWLEntity b, double confidence) {
			this.a = a;
			this.b = b;
			this.confidence = confidence;
		}
	}

