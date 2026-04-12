<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="header.jsp" />

<%
Boolean editMode = (Boolean) request.getAttribute("editMode");
boolean isEdit = editMode != null && editMode;
Map<String, Object> location = (Map<String, Object>) request.getAttribute("location");
%>

<h1><%= isEdit ? "Edit Location" : "Add New Location" %></h1>

<%
String error = (String) request.getAttribute("error");
if (error != null) {
%>
    <div class="error-message"><%= error %></div>
<%
}
%>

<form method="POST" action="<%= request.getContextPath() %><%= isEdit ? "/location/edit" : "/location/new" %>" class="form-card">
<% if (isEdit && location != null) { %>
    <input type="hidden" name="locationId" value="<%= location.get("locationId") %>">
<% } %>
    <label for="name">Location Name</label>
    <input type="text" id="name" name="name" required
           value="<%= location != null && location.get("name") != null ? location.get("name") : "" %>">

    <label for="typeId">Location Type</label>
    <select id="typeId" name="typeId">
        <option value="">Select type...</option>
<%
List<Map<String, Object>> locationTypes = (List<Map<String, Object>>) request.getAttribute("locationTypes");
if (locationTypes != null) {
    for (Map<String, Object> t : locationTypes) {
        boolean selected = location != null && location.get("typeId") != null
            && t.get("typeId").toString().equals(location.get("typeId").toString());
%>
        <option value="<%= t.get("typeId") %>"<%= selected ? " selected" : "" %>><%= t.get("typeName") %></option>
<%
    }
}
%>
    </select>

    <label for="city">City</label>
    <input type="text" id="city" name="city" placeholder="e.g., Sacramento"
           value="<%= location != null && location.get("city") != null ? location.get("city") : "" %>">

    <label for="address">Address</label>
    <input type="text" id="address" name="address" placeholder="e.g., 99 Canal Blvd"
           value="<%= location != null && location.get("address") != null ? location.get("address") : "" %>">

    <label for="description">Description</label>
    <textarea id="description" name="description" rows="4"><%= location != null && location.get("description") != null ? location.get("description") : "" %></textarea>

    <button type="submit" class="btn"><%= isEdit ? "Update Location" : "Add Location" %></button>
</form>

<jsp:include page="footer.jsp" />
