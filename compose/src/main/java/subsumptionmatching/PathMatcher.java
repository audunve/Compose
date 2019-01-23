package compose.matchers;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.RelationshipType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

/**
 * @author audunvennesland
 * 23. jun. 2017 
 */
public class PathMatcher {
	
	public NodeSet<OWLClass> getParents(OWLOntology o, OWLClass cls) {
		
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createReasoner(o);
		
		NodeSet<OWLClass> parents = null;
		
		Set<OWLClassExpression> ex = cls.getSuperClasses(o);
		
		for (OWLClassExpression exp : ex) {
			parents = reasoner.getSuperClasses(exp, true);
		}

				
		return parents;
		
		
	}
	
	/**
	 * This method finds the shortest path between two nodes used as parameters. The path is the full path consisting of nodes and relationships between the classNode..
	 * ...and the parentNode.
	 * @param parentNode
	 * @param classNode
	 * @param label
	 * @param rel
	 * @return Iterable<Path> paths
	 */
	public Iterable<Path> findShortestPathBetweenNodes(Node parentNode, Node classNode, Label label, RelationshipType rel) {

		PathFinder<Path> finder = GraphAlgoFactory.shortestPath(
				PathExpanders.forType(rel), 15);
		Iterable<Path> paths = finder.findAllPaths( classNode, parentNode );
		return paths;

	}

}
