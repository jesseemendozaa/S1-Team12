<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="header.jsp" />

<h1>Add New Location</h1>

<%
String error = (String) request.getAttribute("error");
if (error != null) {
%>
    <div class="error-message"><%= error %></div>
<%
}
%>

<form method="POST" action="<%= request.getContextPath() %>/location/new" class="form-card">
    <label for="name">Location Name</label>
    <input type="text" id="name" name="name" required>

    <label for="typeId">Location Type</label>
    <select id="typeId" name="typeId">
        <option value="">Select type...</option>
<%
List<Map<String, Object>> locationTypes = (List<Map<String, Object>>) request.getAttribute("locationTypes");
if (locationTypes != null) {
    for (Map<String, Object> t : locationTypes) {
%>
        <option value="<%= t.get("typeId") %>"><%= t.get("typeName") %></option>
<%
    }
}
%>
    </select>

    <label for="region">Region</label>
    <input type="text" id="region" name="region" placeholder="e.g., Lake County, CA">

    <label for="description">Description</label>
    <textarea id="description" name="description" rows="4"></textarea>

    <label for="latitude">Latitude</label>
    <input type="number" id="latitude" name="latitude" step="0.0000001" placeholder="e.g., 39.0299">

    <label for="longitude">Longitude</label>
    <input type="number" id="longitude" name="longitude" step="0.0000001" placeholder="e.g., -122.7631">

    <button type="submit" class="btn">Add Location</button>
</form>

<jsp:include page="footer.jsp" />
