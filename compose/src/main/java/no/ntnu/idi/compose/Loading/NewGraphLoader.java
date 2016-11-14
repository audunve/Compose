package no.ntnu.idi.compose.Loading;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.UniqueFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class NewGraphLoader {
	
	File dbFile = new File("/Users/audunvennesland/Documents/PhD/Development/Neo4J/TestFull");
	GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
	Label label = DynamicLabel.label("Test");
	

	private void importOntology(OWLOntology ontology) {
		
		OWLReasoner reasoner = new Reasoner(ontology);

		try ( Transaction tx = db.beginTx() )
		{
		
        	//creating a node for owl:Thing
			Node thingNode = db.createNode(label);
			thingNode.setProperty("classname", "owl:Thing");
			System.out.println("Creating owl:thing node " + thingNode.toString() + " with classname owl:Thing");
			
			for (OWLClass c : ontology.getClassesInSignature(true)) {
	               String classString = c.toString();
	               if (classString.contains("#")) {
	                    classString = classString.substring(
	                    classString.indexOf("#")+1,classString.lastIndexOf(">"));
	               }
	               	Node classNode = db.createNode(label);
	               	classNode.setProperty("classname", classString);
	               	System.out.println("Creating class node " + classNode.toString() + " with classname " + classString);
	               	System.out.println("This node has ID " + classNode.getId());
	               	
	               	NodeSet<OWLClass> superclasses = reasoner.getSuperClasses(c, true);
                    if (superclasses.isEmpty()) {
                    	System.out.println("The node " + classNode.toString() + " does not have any superclasses");
                         classNode.createRelationshipTo(thingNode,
                         DynamicRelationshipType.withName("isA"));
                         } else {
                         for (org.semanticweb.owlapi.reasoner.Node<OWLClass>
                         parentOWLNode: superclasses) {
                              OWLClassExpression parent =
                              parentOWLNode.getRepresentativeElement();
                              String parentString = parent.toString();
                              if (parentString.contains("#")) {
                                   parentString = parentString.substring(
                                   parentString.indexOf("#")+1,
                                   parentString.lastIndexOf(">"));
                              }
                              
                              //need to find a way of checking if a node already exists
                             
                              Node parentNode = db.createNode(label);
                              parentNode.setProperty("classname", parentString);
                              System.out.println("Creating parent node " + parentNode.toString() + " with classname " + parentString);
                              
                              classNode.createRelationshipTo(parentNode,
                                  DynamicRelationshipType.withName("isA"));
                         }
                    
                    
                    for (org.semanticweb.owlapi.reasoner.Node<OWLNamedIndividual> in
                            : reasoner.getInstances(c, true)) {
                            OWLNamedIndividual i = in.getRepresentativeElement();
                            String indString = i.toString();
                            if (indString.contains("#")) {
                                 indString = indString.substring(
                                      indString.indexOf("#")+1,indString.lastIndexOf(">"));
                            }
                            Node individualNode = db.createNode(label);
                            
                            individualNode.setProperty("classname", indString);
                            System.out.println("Creating individual node " + individualNode.toString() + " with classname " + indString);
                            individualNode.createRelationshipTo(classNode,
                            DynamicRelationshipType.withName("isA"));
                            
                            for (OWLObjectPropertyExpression objectProperty:
                                ontology.getObjectPropertiesInSignature()) {
                                     for
                                     (org.semanticweb.owlapi.reasoner.Node<OWLNamedIndividual>
                                     object: reasoner.getObjectPropertyValues(i,
                                     objectProperty)) {
                                          String reltype = objectProperty.toString();
                                          reltype = reltype.substring(reltype.indexOf("#")+1,
                                          reltype.lastIndexOf(">"));
                                          String s =
                                          object.getRepresentativeElement().toString();
                                          s = s.substring(s.indexOf("#")+1,
                                          s.lastIndexOf(">"));
                                          Node objectNode = db.createNode(label);
                                          objectNode.setProperty("classname", reltype);
                                          System.out.println("Creating object node " + objectNode.toString() + " with classname " + reltype);
                                          individualNode.createRelationshipTo(objectNode,
                                          DynamicRelationshipType.withName(reltype));
                                     }
                               }
                               for (OWLDataPropertyExpression dataProperty:
                               ontology.getDataPropertiesInSignature()) {
                            	   
                                     for (OWLLiteral object: reasoner.getDataPropertyValues(
                                     i, dataProperty.asOWLDataProperty())) {
                                          String reltype =
                                          dataProperty.asOWLDataProperty().toString();
                                          reltype = reltype.substring(reltype.indexOf("#")+1, 
                                          reltype.lastIndexOf(">"));
                                          String s = object.toString();
                                          individualNode.setProperty(reltype, s);
                                     }
                               }
                          }
                     }
                    tx.success();
			}
        }
	}
	
	public static void main(String[] args) throws OWLOntologyCreationException {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File f1 = new File("/Users/audunvennesland/Documents/PhD/Ontologies/TestOntologiesTransport/TestTransport1.owl");		
		
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(f1);
		
		NewGraphLoader loader = new NewGraphLoader();
		
		loader.importOntology(onto1);
		
	}
}
	
