package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.*;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {
private Graph<Fermata, DefaultEdge> graph;
private List<Fermata> fermate;
private Map<Integer, Fermata> fermateIdMap;
private TraversalListener<Fermata, DefaultEdge> l;
public Model() {
	this.graph= new SimpleDirectedGraph<>(DefaultEdge.class);
	MetroDAO dao=new MetroDAO();
	//CREO VERTICI	
	this.fermate=dao.getAllFermate();
	this.fermateIdMap=new HashMap<>();
	for (Fermata f:this.fermate) {
		fermateIdMap.put(f.getIdFermata(),f);
	}
	Graphs.addAllVertices(this.graph, this.fermate);
	System.out.println(this.graph);
	
	for (Fermata fp: this.fermate) {
		for (Fermata fa: this.fermate) {
			if (dao.fermateConnesse(fp, fa))
				this.graph.addEdge(fp,fa);
		}
	}
	/*CREO ARCHI metodo due
	for (Fermata fp:this.fermate) {
		List<Fermata> connesse=dao.fermateSuccessive(fp, fermateIdMap);
		for (Fermata fa: connesse) {
			this.graph.addEdge(fp, fa);
		}
	}*/
	//creo archi metodo tre (chiedo al DB elenco archi)
	List<CoppiaFermate> coppie= dao.coppieFermate(fermateIdMap);
	for (CoppiaFermate c: coppie) {
		this.graph.addEdge(c.getFp(), c.getFa());
	}
}
public List<Fermata> VisitaAmpiezza(Fermata source){
	List<Fermata> visita=new ArrayList<>();
	BreadthFirstIterator<Fermata, DefaultEdge> bfv= new BreadthFirstIterator(this.graph);
	while(bfv.hasNext()) {
		visita.add(bfv.next());
	}
	
	return visita;
}
//visita in ampiezza vede le collegate alla fonte e le colllegate alle collegate



public List<Fermata> VisitaProfondita(Fermata source){
	List<Fermata> visita=new ArrayList<>();
	DepthFirstIterator<Fermata, DefaultEdge> dfv= new DepthFirstIterator(this.graph);
	while(dfv.hasNext()) {
		visita.add(dfv.next());
	}
	
	return visita;
}
//albero di visita mappa il vertice con il precedente, è una mappa fermata fermata!!
public Map<Fermata,Fermata> alberoVisita(Fermata source) {
	Map<Fermata, Fermata> albero=new HashMap<>();
	albero.put(source,null);
	GraphIterator<Fermata,DefaultEdge> bfv=new BreadthFirstIterator<>(graph);
	bfv.addTraversalListener(new TraversalListener<Fermata,DefaultEdge>(){

		@Override
		public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> e) {
			//la visita sta considerando un arco, mi chiedo se questo
			//arco ha scoperto un nuovo vertice se si provenendo da dove?
			DefaultEdge edge=e.getEdge();//arco di tippon (a,b)
			Fermata a=graph.getEdgeSource(edge);
			Fermata b=graph.getEdgeTarget(edge);
			if (albero.containsKey(a)) {
				albero.put(b, a);
			}
			else {
				albero.put(a, b);
			}
		}

		@Override
		public void vertexTraversed(VertexTraversalEvent<Fermata> e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void vertexFinished(VertexTraversalEvent<Fermata> e) {
			// TODO Auto-generated method stub
			
		}
		
	});
	while(bfv.hasNext()) {
		bfv.next(); // estrai l'elemento ma in questo caso ignoralo
	}
	
	return albero;
	}

public List<Fermata> camminiMinimi(Fermata partenza, Fermata arrivo) {
	DijkstraShortestPath <Fermata, DefaultEdge> dij= new DijkstraShortestPath <Fermata, DefaultEdge>(graph);
	GraphPath<Fermata, DefaultEdge> cammino =dij.getPath(partenza, arrivo);
	return cammino.getVertexList();
}

public static void main(String args[]) {
	Model m=new Model();
	List<Fermata> visita=m.VisitaAmpiezza(m.fermate.get(0));
	System.out.println(visita);
	Map<Fermata, Fermata> albero= m.alberoVisita(m.fermate.get(0));
	for (Fermata f: albero.keySet()) {
		System.out.format ("%s <- %s",f,albero.get(f));
	}
	List<Fermata> cammino= m.camminiMinimi(m.fermate.get(0), m.fermate.get(1));
	System.out.println(cammino);
}
}
