<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="header.jsp" />

<%
Map<String, Object> species = (Map<String, Object>) request.getAttribute("species");
String error = (String) request.getAttribute("error");
%>

<h1>Edit Species</h1>

<% if (error != null) { %>
    <div class="error-message"><%= error %></div>
<% } %>

<form method="POST" action="<%= request.getContextPath() %>/species/edit" class="form-card">
    <input type="hidden" name="speciesId" value="<%= species.get("speciesId") %>">

    <label for="commonName">Common Name</label>
    <input type="text" id="commonName" name="commonName" required
           value="<%= species.get("commonName") != null ? species.get("commonName") : "" %>">

    <label for="scientificName">Scientific Name</label>
    <input type="text" id="scientificName" name="scientificName"
           value="<%= species.get("scientificName") != null ? species.get("scientificName") : "" %>">

    <button type="submit" class="btn">Update Species</button>
</form>

<jsp:include page="footer.jsp" />
