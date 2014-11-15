package com.cfm.productline.solver;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.cfm.common.AbstractModel;
import com.cfm.hlcl.Domain;
import com.cfm.hlcl.HlclProgram;
import com.cfm.hlcl.LiteralBooleanExpression;
import com.cfm.jgprolog.core.CompoundTerm;
import com.cfm.jgprolog.core.IntegerTerm;
import com.cfm.jgprolog.core.ListTerm;
import com.cfm.jgprolog.core.PrologContext;
import com.cfm.jgprolog.core.PrologEngine;
import com.cfm.jgprolog.core.PrologException;
import com.cfm.jgprolog.core.PrologTermFactory;
import com.cfm.jgprolog.core.QueryResult;
import com.cfm.jgprolog.core.Term;
import com.cfm.jgprolog.core.VariableSet;
import com.cfm.jgprolog.core.VariableTerm;
import com.cfm.productline.Asset;
import com.cfm.productline.ProductLine;
import com.cfm.productline.VariabilityElement;
import com.cfm.productline.productLine.Pl2Hlcl;
import com.cfm.productline.prologEditors.Hlcl2GnuPrologExact;
import com.cfm.productline.prologEditors.PrologTransformParameters;

public class PrologSolver implements Solver{
	
	public static final int DOMAIN_SIZE_TOP = 50;
	
	private PrologEngine prolog ;
	private PrologTermFactory ptf ;
	private AbstractModel pl;
	private QueryResult qr;
	
	public PrologSolver(PrologContext ctx){
		prolog = ctx.getEngine();
		ptf = ctx.getTermFactory();
	}
	
	public static void consultString(PrologEngine prolog, String file) throws PrologException{
		try {
			// Create a temporary file
			File f = File.createTempFile("tmp", ".pl");
			f.deleteOnExit();

			PrintWriter w = new PrintWriter(f);
			w.println(file);
			w.close();

			// Consult it with prolog
			prolog.consult(f.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static CompoundTerm addSubQueries(List<CompoundTerm> parts, PrologTermFactory ptf){
		
		if( parts.size() == 0 )
			return null;
		
		if( parts.size() == 1 )
			return parts.get(0);
		
		int n = parts.size();
		
		CompoundTerm partial = ptf.newCompound(",", parts.get(n - 2), parts.get(n - 1) );
		for(int i = n - 3; i >= 0; i--)
			partial = ptf.newCompound(",", parts.get(i), partial);
		
		return partial;
	}
//	private static void writeIdentifiersList(Set<String> ids, StringBuffer out){
//		int i = 0;
//		for(String id : ids){
//			out.append(id);
//			i++;
//			if( i < ids.size() )
//				out.append(",");
//		}
//	}
	
	@Override
	public int getSolutionsCount() {
		return 0;
	}

	@Override
	public void setProductLine(AbstractModel pl) {
		this.pl = pl;
	}

	@Override
	public void solve(Configuration config, ConfigurationOptions options) {
		if(options.isStartFromZero()){
			endSolving();
			try {
				doQuery(config, options);
			} catch (PrologException e) {
				e.printStackTrace();
			}
			return ;
		}
		if( qr == null ){
			try {
				doQuery(config, options);
			} catch (PrologException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean hasNextSolution() {
		return qr.hasMoreSolutions();
	}

	@Override
	public Configuration getSolution() {
		if( qr == null ){
			return null;
		}
		
		VariableSet set = qr.getVariableSet();
		
		//Create the solution.
		Configuration c = new Configuration();
		for( String id : set.keySet() ){
			VariableTerm var = set.getVariable(id);
			IntegerTerm it = (IntegerTerm)var.getValue();
			
			if( it.getValue() == 1)
				c.enforce(var.getName());
			else
				c.ban(var.getName());
		}
		qr.nextSolution();
		return c;
	}

	private PrologTransformParameters getParamsFor(ConfigurationOptions options){
		PrologTransformParameters params = new PrologTransformParameters();
		
		params.setFdLabeling(options.getMode() == ConfigurationMode.FULL);
		
		return params;
	}
	
	private PrologTransformParameters addParametersToProgram(HlclProgram prog, ConfigurationOptions options){
		PrologTransformParameters params = getParamsFor(options);
		
		for(String str : options.getAdditionalConstraints()){
			prog.add(new LiteralBooleanExpression(str));
		}
		
		if( params.isFdLabeling() )
			prog.add(new LiteralBooleanExpression("fd_labeling(L)"));
		
		return params;
	}
	
	private void doQuery(Configuration config, ConfigurationOptions options) throws PrologException {
		prolog.startProlog();

		//Generate the product line program.
		Hlcl2GnuPrologExact hlcl2gnuProlog = new Hlcl2GnuPrologExact();
		
		HlclProgram prog = Pl2Hlcl.transformExact((ProductLine)pl);
		PrologTransformParameters params = addParametersToProgram(prog, options);

		String str = hlcl2gnuProlog.transform(prog, params);
		System.out.println("================================");
		System.out.println(str);
		System.out.println("================================");
		try {
			consultString(prolog, str);
		} catch (PrologException e) {
			e.printStackTrace();
		}
		
		//Generate the configurationQuery.
		//Generate the variables.
		Set<String> ids = new TreeSet<>();
		Map<String, VariableTerm> vars = new TreeMap<>();
		for(VariabilityElement elm : pl.getVariabilityElements()){
			//ids.add(elm.getIdentifier());
			//vars.put(elm.getIdentifier(), ptf.newVariable(elm.getIdentifier()));
			ids.add(elm.getName());
			vars.put(elm.getName(), ptf.newVariable(elm.getName()));
		}
		//todo: jcmunoz should be threated in the same way
		for(Asset a : pl.getAssets().values()){
			ids.add(a.getIdentifier());
			vars.put(a.getIdentifier(), ptf.newVariable(a.getIdentifier()));
		}
		
		VariableTerm[] varTermsArray = new VariableTerm[vars.size()];
		int count = 0;
		for(String id : vars.keySet()){
			varTermsArray[count] = vars.get(id);
			count++;
		}
		//Generate the list
		ListTerm L = ptf.newList( varTermsArray );
		
		List<CompoundTerm> parts = new ArrayList<>();
		//Generate the assigns for the not ignored variables
		for (String id : config.getNotIgnored() ){
			//Create the atom
			IntegerTerm i = ptf.newInteger(config.stateOf(id));
			//Create the compound for the assign.
			CompoundTerm assign = ptf.newCompound("=", vars.get(id), i);
			parts.add(assign);
		}
		
		parts.add( ptf.newCompound("productline", L) );
		
		CompoundTerm query = addSubQueries(parts, ptf);
		System.out.println(query.toString());
		qr = prolog.runQuery(query);
	}

	public void endSolving() {
		if( qr != null ){
			try {
				qr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			qr = null;
			prolog.stopProlog();
		}
	}

	@Override
	public void nextSolution() {
		if(hasNextSolution())
			qr.nextSolution();
		else
			endSolving();
	}

	@Override
	public AbstractModel getProductLine() {
		return pl;
	}

	@Override
	public Map<String, List<Integer>> reduceDomain(Configuration config, ConfigurationOptions options) {
		options.setMode(ConfigurationMode.PARTIAL);
		solve(config, options);
		
		VariableSet vs = null;
		Map<String, List<Integer>> domains = new HashMap<String, List<Integer>>();
		//Get L
		if(hasNextSolution()){
			vs = qr.getVariableSet();
			
			for( String str : vs.keySet() ){
				List<Integer> dom = getDomainFor(vs.getVariable(str));
				if( dom != null )
					domains.put(str, dom);
			}
			try {
				qr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		qr = null;
				
		return domains;
	}
	
	private List<Integer> getDomainFor(VariableTerm X){
		//Get the size
		int size = getDomainSize(X);
		
		//if size  > 50 don't change the domain
		if( size > DOMAIN_SIZE_TOP )
			return null;
		
		//return the domain
		return getDomainValues(X);
	}
	
	private int getDomainSize(VariableTerm X){
		VariableTerm N = ptf.newVariable("N");
		CompoundTerm query = ptf.newCompound("fd_size", X, N);
		int r = -1;
		try(QueryResult qr = prolog.runQuery(query)){
			if( qr.hasMoreSolutions() ){
				IntegerTerm it = (IntegerTerm) N.getValue();
				r = it.getValue();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PrologException e) {
			e.printStackTrace();
		}
		return r;
	}
	
	private List<Integer> getDomainValues(VariableTerm X){
		VariableTerm L = ptf.newVariable("L");
		CompoundTerm query = ptf.newCompound("fd_dom", X, L);
		List<Integer> dom = new ArrayList<>();
		try(QueryResult qr = prolog.runQuery(query)){
			if( qr.hasMoreSolutions() ){
				ListTerm it = (ListTerm) L.getValue();
				for(Term t : it.getAllTerms()){
					dom.add( ((IntegerTerm)t).getValue() );
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PrologException e) {
			e.printStackTrace();
		}
		return dom;
	}
	
}
