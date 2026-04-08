<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="header.jsp" />

<%
Boolean editMode = (Boolean) request.getAttribute("editMode");
boolean isEdit = editMode != null && editMode;
Map<String, Object> report = (Map<String, Object>) request.getAttribute("report");
%>

<h1><%= isEdit ? "Edit Report" : "Log a Catch" %></h1>

<%
String error = (String) request.getAttribute("error");
if (error != null) {
%>
    <div class="error-message"><%= error %></div>
<%
}
%>

<form method="POST" action="<%= request.getContextPath() %><%= isEdit ? "/report/edit" : "/report/new" %>" class="form-card">
<% if (isEdit && report != null) { %>
    <input type="hidden" name="reportId" value="<%= report.get("reportId") %>">
<% } %>

    <label for="locationId">Location</label>
    <select id="locationId" name="locationId" required>
        <option value="">Select location...</option>
<%
    List<Map<String, Object>> locations = (List<Map<String, Object>>) request.getAttribute("locations");
    String selectedLocationId = request.getAttribute("selectedLocationId") != null
        ? request.getAttribute("selectedLocationId").toString() : null;
    if (locations != null) {
        for (Map<String, Object> loc : locations) {
            String locId = loc.get("locationId").toString();
            boolean selected = false;
            if (isEdit && report != null) {
                selected = locId.equals(report.get("locationId").toString());
            } else if (selectedLocationId != null) {
                selected = locId.equals(selectedLocationId);
            }
%>
        <option value="<%= locId %>"<%= selected ? " selected" : "" %>><%= loc.get("name") %></option>
<%
        }
    }
%>
    </select>

    <label for="speciesId">Species</label>
    <select id="speciesId" name="speciesId" required>
        <option value="">Select species...</option>
<%
    List<Map<String, Object>> species = (List<Map<String, Object>>) request.getAttribute("species");
    if (species != null) {
        for (Map<String, Object> sp : species) {
            String spId = sp.get("speciesId").toString();
            boolean selected = isEdit && report != null && spId.equals(report.get("speciesId").toString());
%>
        <option value="<%= spId %>"<%= selected ? " selected" : "" %>><%= sp.get("commonName") %></option>
<%
        }
    }
%>
    </select>

    <label for="catchDate">Catch Date</label>
    <input type="date" id="catchDate" name="catchDate" required
           value="<%= isEdit && report != null && report.get("catchDate") != null ? report.get("catchDate") : "" %>">

    <label for="weightLbs">Weight (lbs)</label>
    <input type="number" id="weightLbs" name="weightLbs" step="0.01" min="0"
           value="<%= isEdit && report != null && report.get("weightLbs") != null ? report.get("weightLbs") : "" %>">

    <label for="lengthInches">Length (inches)</label>
    <input type="number" id="lengthInches" name="lengthInches" step="0.01" min="0"
           value="<%= isEdit && report != null && report.get("lengthInches") != null ? report.get("lengthInches") : "" %>">

    <label for="method">Method / Time of Day</label>
    <input type="text" id="method" name="method"
           value="<%= isEdit && report != null && report.get("method") != null ? report.get("method") : "" %>">

    <label for="notes">Notes</label>
    <textarea id="notes" name="notes" rows="4"><%= isEdit && report != null && report.get("notes") != null ? report.get("notes") : "" %></textarea>

    <button type="submit" class="btn"><%= isEdit ? "Update Report" : "Submit Report" %></button>
</form>

<% if (isEdit && report != null) { %>
    <form method="POST" action="<%= request.getContextPath() %>/report/edit" class="inline-form"
          onsubmit="return confirm('Are you sure you want to delete this report?');">
        <input type="hidden" name="reportId" value="<%= report.get("reportId") %>">
        <input type="hidden" name="action" value="delete">
        <button type="submit" class="btn btn-danger">Delete Report</button>
    </form>
<% } %>

<jsp:include page="footer.jsp" />
