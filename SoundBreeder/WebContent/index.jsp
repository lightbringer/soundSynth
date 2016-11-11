<%@page import="se.lu.lucs.sound.SoundBreeder"%>
<%@page import="se.lu.lucs.sound.BoutParameters"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>

<jsp:useBean id="engine" scope="session"
	class="se.lu.lucs.sound.SoundBreeder" />

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<%
	List<BoutParameters> par;
	if(request.getParameter( "reset" ) != null ){
	    engine.reset();
	}
	else if(request.getParameter("evolving") != null){
	    par = (List<BoutParameters>)session.getAttribute( "boutParameters");
	    if(par == null){
	        par = engine.getPopulation(); //Should not happen
	    }
	    List<BoutParameters> select = new ArrayList<BoutParameters>();
	    for(int i = 0; i < SoundBreeder.POPULATION_SIZE; i++){
	        String s = request.getParameter( i+"_select" );
	       	if(s != null){
	       	    select.add(par.get(i));
	       	}
	    }
	    %> Using <%=select.size() %> sounds to form new ones <%
	    engine.setPopulation(select);
	    engine.advance();
	}
    par = engine.getPopulation();
    session.setAttribute( "boutParameters", par );
%>
<body>
	<form method="POST">
		<fieldset>
			<div style="background-color: #444444;">
				<%
				    for (int i = 0; i < par.size(); i++) {
				%><div style="background-color: #FF0000;">

					&nbsp;
					<audio controls type="audio/wav" src="<%=i%>.wav?t=<%= Math.random( )%>"></audio>
					<input type="checkbox" name="<%=i%>_select"></input>
					<%=par.get( i ).toString()%>
				</div>
				<%
				    }
				%>
			</div>
		</fieldset>
		<input type="checkbox" name="reset">Start Over</input>
		<input type="submit" />
		<input type="hidden" name="evolving" value="true" />
	</form>
</body>
</html>