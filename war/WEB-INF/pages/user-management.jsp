<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<t:page>
	<jsp:attribute name="content">
		<div class="content card bg-brandeis-white">
			<div data-editable class="section content-tools-editable">
		
				<p>
					Currently logged in as ${currentUser.nickname} (${currentUser.email}). 
				</p>
				<p>
					Click here to <a href="${logoutUrl}">Log Out</a> or to <a href="/console">return to the console</a>.
				</p>
				<br>
				<br>
				<h1>Brandeis Student Union Website : User Manager</h1>
				<br>
				<br>
				<div>
					<h1> Owners</h1>
					<p>
						Owners are people who are able to do ANYTHING on the site.
						This includes adding and removing administrators and owners, and overseeing the forums.
						All owners are automatically made administrators, nobody can be an owner and not an administrator.
						Ownership should probably not be given to more than a dozen people, just to make sure that the organization
						maintains a strong sense of order and control.
						Note that most site functions (i.e. changing site pages) can be done by administrators, so only a few
						people need to be given ownership permissions.
					</p>
					<p>
						Worse comes to worse, if you cannot get an existing owner to add you (or perhaps you deleted yourself as an owner)
						reach out to Grady Ward, he can add you as an owner.
					</p>
					<p>
						In this section, site owners can remove other site owners (or themselves) by clicking on the button next to each.
						This action cannot be undone (though one could add the email as an owner again).
					</p>
					<ul>
						<c:forEach items="${owners}" var="owner">
							<li>
								<b>${owner.nickname}</b> - 
								<a href="mailto:${owner.email}">${owner.email}</a>
								<c:if test="${isOwner}">
									<form action="/users" method="POST">
										<input type="hidden" name="email" value="${owner.email}">
										<input type="hidden" name="addOrRemove" value="remove">
										<input type="hidden" name="adminOrOwnerOrCandidate" value="owner">
										<button>Remove As Owner (But Not as Administrator)</button>
									</form>
								</c:if>
							</li>
						</c:forEach>
					</ul>
					<c:if test="${isOwner}">
						<h3>Add Owner</h3>
						<p>
							Add the email of the Owner (NO WHITESPACES, NO MULTIPLE EMAILS) to the field below, and click submit.
							Note that changes can take up to a minute to be reflected on this page. 
							If prompted, do not send the form again, rather, reload the page without using the back buttons.
						</p>
						<form action="/users" method="POST">
							<input type="text" name="email" placeholder="email@address.here">
							<input type="hidden" name="addOrRemove" value="add">
							<input type="hidden" name="adminOrOwnerOrCandidate" value="owner">
							<button>Add Owner By Email</button>
						</form>
					</c:if>
				</div>
				<br>
				<div>
					<h1>Administrators</h1>
					<p> 
						Administrators are people who can edit site pages, and moderate the forums.
						Any administrator can modify any page on the site. However, remember that
						<b>any changes that you make are documented as yours</b> meaning, that if you
						post something that shouldn't be up, there WILL be a record of that.
						Administrators can be added by Owners. You can contact a site owner to request
						to be added.
					</p>
					<p>
						In this section, site owners can remove administrators by clicking on the button next to each.
						This action cannot be undone (though one could add the email as an administrator again).
					</p>
					<ul>
						<c:forEach items="${admins}" var="admin">
							<li>
								<b>${admin.nickname}</b> - 
								<a href="mailto:${admin.email}">${admin.email}</a>
								<c:if test="${isOwner}">
									<form action="/users" method="POST">
										<input type="hidden" name="email" value="${admin.email}">
										<input type="hidden" name="addOrRemove" value="remove">
										<input type="hidden" name="adminOrOwnerOrCandidate" value="admin">
										<button>Remove As Administrator</button>
									</form>
								</c:if>
							</li>
						</c:forEach>
					</ul>
					<c:if test="${isOwner}">
						<h3>Add Admin</h3>
						<p>
							Add the email of the Administrator (NO WHITESPACES, NO MULTIPLE EMAILS) to the field below, and click submit.
							Note that changes can take up to a minute to be reflected on this page. 
							If prompted, do not send the form again, rather, reload the page without using the back buttons.
						</p>
						<form action="/users" method="POST">
							<input type="text" name="email" placeholder="email@address.here">
							<input type="hidden" name="addOrRemove" value="add">
							<input type="hidden" name="adminOrOwnerOrCandidate" value="admin">
							<button>Add Administrator By Email</button>
						</form>
					</c:if>
				</div>
				<br>
				<div>
					<h1>Candidates</h1>
					<p> 
						Candidates are potential Union Members. They are given the chance to create a 
						profile (with a name, class year, biography and photo), and can be displayed 
						on the elections page. Candidates can only be created or deleted by site owners.
						Candidates cannot edit Union Pages, they only have access to their own profile.
					</p>
					<p>
						In this section, site owners can remove candidates by clicking on the button next to each.
						This action cannot be undone (though one could add the email as a candidate again).
					</p>
					<ul>
						<c:forEach items="${candidates}" var="candidate">
							<li>
								<b>${candidate.nickname}</b> - 
								<a href="mailto:${candidate.email}">${candidate.email}</a>
								<c:if test="${isOwner}">
									<form action="/users" method="POST">
										<input type="hidden" name="email" value="${candidate.email}">
										<input type="hidden" name="addOrRemove" value="remove">
										<input type="hidden" name="adminOrOwnerOrCandidate" value="candidate">
										<button>Remove as Candidate</button>
									</form>
								</c:if>
							</li>
						</c:forEach>
					</ul>
					<c:if test="${isOwner}">
						<h3>Add Candidate</h3>
						<p>
							Add the email of the Candidate (NO WHITESPACES, NO MULTIPLE EMAILS) to the field below, and click submit.
							Note that changes can take up to a minute to be reflected on this page. 
							If prompted, do not send the form again, rather, reload the page without using the back buttons.
						</p>
						<form action="/users" method="POST">
							<input type="text" name="email" placeholder="email@address.here">
							<input type="hidden" name="addOrRemove" value="add">
							<input type="hidden" name="adminOrOwnerOrCandidate" value="candidate">
							<button>Add Candidate By Email</button>
						</form>
					</c:if>
				</div>

        	</div>
		</div>
	</jsp:attribute>
</t:page>