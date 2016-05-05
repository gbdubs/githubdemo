package subrandeis.servlet.adv;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import subrandeis.api.GithubAPI;
import subrandeis.api.Log;
import subrandeis.api.ObjectifyAPI;
import subrandeis.api.SecretsAPI;
import subrandeis.api.UserAPI;
import subrandeis.entities.Group;
import subrandeis.entities.Page;
import subrandeis.entities.Person;
import subrandeis.servlet.basic.JSPRenderServlet;

import com.googlecode.objectify.Objectify;

@SuppressWarnings("serial")
public class GroupManagerServlet extends HttpServlet{

	static Objectify ofy = ObjectifyAPI.ofy();

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException{
		Group g = Group.get(req.getParameter("groupId"));
			boolean hasPermission = isGroupLeader(g);
			if (g != null){
				hasPermission = hasPermission || g.leaders.contains(p.email);
				if (hasPermission){
					// Sets the atributes that are rendered.
					req.setAttribute("group", g);
					req.setAttribute("currentUser", p);
					req.setAttribute("logoutUrl", UserAPI.logoutUrl());
					
					// Finishes up, sends to the console page.
					resp.setContentType("text/html");
					RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/pages/group-manager.jsp");
					jsp.forward(req, resp);	
					return;
				}
			} else {
				hasPermission = hasPermission || UserAPI.isAdmin();
				if (hasPermission){
					req.setAttribute("groups", Group.getAllGroups());
					req.setAttribute("currentUser", p);
					req.setAttribute("logoutUrl", UserAPI.logoutUrl());
					
					// Finishes up, sends to the console page.
					resp.setContentType("text/html");
					RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/pages/group-manager-list.jsp");
					jsp.forward(req, resp);	
					return;
				}
			}
			String error = "There was an error in rendering the group manager for the group requested with id [%s]. The UserAPI was [%s].";
			error = String.format(error, req.getParameter("groupId"), UserAPI.email());
			Log.warn(error);
			resp.getWriter().println(error);
			return;
		}
		resp.sendRedirect("/login-admin?goto=%2Fgroup-manager");
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException{
		String manage = req.getParameter("manage");
		
		if (manage == null){
			resp.getWriter().println("Missing parameter [manage].\n");
		}
		
		if ("nameOrPageUrlOrDescription".equals(manage)){
			doNamePageUrlDescriptionManagement(req, resp);
		} else if ("roles".equals(manage)){
			doRoleManagement(req, resp);
		} else if ("members".equals(manage)){
			doMemberManagement(req, resp);
		} else if ("updatePage".equals(manage)){
			doPageUpdate(req, resp);
		} else {
			resp.getWriter().println("Incorrect value for parameter [manage] passed.");
		}		
	}
	
	private void doPageUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		String groupId = req.getParameter("groupId");
		if (UserAPI.loggedIn()){
			Person p = Person.get(UserAPI.email());
			Group g = Group.get(groupId);
			if (g != null && p != null && (UserAPI.isGoogleAdmin() || p.owner || g.leaders.contains(p.email) || g.members.contains(p.email))){
				updateMembershipPage(g, req, resp);
				Log.info(String.format("User [%s] successfully triggered an update of the group [%s][%s].", p.email, g.name, g.id));	
				resp.sendRedirect("/group-manager?groupId="+groupId);
				return;
			}
			String response = String.format("Something is wrong in the name and page url management of the group. The incorrect call was made by uaser [%s]\n", UserAPI.email());
			Log.warn(response);
			resp.getWriter().println(response);
			return;
		}
		resp.sendRedirect("/login-admin?goto=%2Fgroup-manager");
	}

	public void doNamePageUrlDescriptionManagement(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		String groupId = req.getParameter("groupId");
		if (UserAPI.loggedIn()){
			Person p = Person.get(UserAPI.email());
			Group g = Group.get(groupId);

			if (g != null && p != null && (UserAPI.isGoogleAdmin() || p.owner || g.leaders.contains(p.email))){
				String name = req.getParameter("name");
				String pageURL = req.getParameter("pageUrl");
				String description = req.getParameter("description");
				if (name != null && pageURL != null){
					g.name = name;
					g.pageUrl = pageURL;
					g.description = description;
					ofy.save().entity(g);
					Log.info(String.format("Successfully updated the name and page URL and description of group [%s] to [%s], [%s] and [%s] respectively.\n", g.id, g.name, g.pageUrl, g.description));	
					resp.sendRedirect("/group-manager?groupId="+groupId);
					return;
				} 
				resp.getWriter().println("Either the name or pageURL was not passed properly. Check your API Call.\n");
				return;
			}
			Log.warn(String.format("Something is wrong in the name and page url management of the group. The incorrect call was made by uaser [%s]\n", UserAPI.email()));
			return;
		}
		resp.sendRedirect("/login-admin?goto=%2Fgroup-manager");
	}
	
	public void doRoleManagement(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		
		if (UserAPI.loggedIn()){
		
			String groupId = req.getParameter("groupId");
			Person p = Person.get(UserAPI.email());
			Group g = Group.get(groupId);

			if (g != null && p != null && (UserAPI.isGoogleAdmin() || p.owner || g.leaders.contains(p.email))){
				int roleIndex = 0;
				Map<String, String> roles = new HashMap<String, String>();
				while (req.getParameter("role" + roleIndex) != null){
					String emailX = req.getParameter("email" + roleIndex);
					String roleX = req.getParameter("role" + roleIndex);
					roles.put(emailX, roleX);
					roleIndex++;
				}
				g.roles = roles;
				ofy.save().entity(g);
				Log.info(String.format("Updated the roles for group [%s][%s].", g.id, g.name));
				resp.sendRedirect("/group-manager");
				return;
			}
			String warning = String.format("UserAPI [%s] attempted to update roles for group [%s][%s], but was turned away for insufficent permissions.\n", p.email, g.id, g.name);
			Log.warn(warning);
			resp.getWriter().println(warning);
			return;
		}
		resp.sendRedirect("/login-admin?goto=%2Fgroup-manager");
	}
	
	public void doMemberManagement(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException{	
		
		String addOrRemove = req.getParameter("addOrRemove");
		String leaderOrMember = req.getParameter("leaderOrMember");
		String groupUuid = req.getParameter("groupId");
		String emailsAsString = req.getParameter("emails");
		
		if (addOrRemove != null && leaderOrMember != null && emailsAsString != null && groupUuid != null && UserAPI.loggedIn()){
			Group g = Group.get(groupUuid);
			Person p = Person.get(UserAPI.email());
			if (g != null && p != null && (p.owner || UserAPI.isGoogleAdmin() || g.leaders.contains(p.email))){
				boolean changed = false;
				
				String[] emails = emailsAsString.split(",");
				List<String> trimmedLowerCaseEmails = new ArrayList<String>();
				for (String s : emails){
					trimmedLowerCaseEmails.add(s.toLowerCase().trim());
				}
				
				if (addOrRemove.equals("add")){
					if (leaderOrMember.equals("leader")){
						changed = g.addLeaders(trimmedLowerCaseEmails);
					} else if (leaderOrMember.equals("member")){
						changed = g.addMembers(trimmedLowerCaseEmails);
					}
				} else if (addOrRemove.equals("remove")){
					if (leaderOrMember.equals("leader")){
						changed = g.removeLeaders(trimmedLowerCaseEmails);
					} else if (leaderOrMember.equals("member")){
						changed = g.removeMembers(trimmedLowerCaseEmails);
					}
				}
				
				if (changed){
					updateMembershipPage(g, req, resp);
				}
				
				resp.sendRedirect("/group-manager");
				return;
			}
			
			String permissionsError = "UserAPI [%s] attempted to modify group membership for group [%s][%s], but was blocked for lack of permissions.\n";
			permissionsError = String.format(permissionsError, p.email, g.id, g.name);
			Log.warn(permissionsError);
			resp.getWriter().println(permissionsError);
			return;
		}
		
		String warning = "Incorrect API Call to Group Manager Servlet.  Check Endpoints and make sure you are logged in.\n";
		Log.warn(warning);
		resp.getWriter().println(warning);
		
	}

	/**
	 * Updates the group membership page for a given group.
	 * Must be called from a servlet.
	 * @param caller Servlet that calls this (needed for referencing the HTML templates)
	 * @throws ServletException 
	 */
	public static void updateMembershipPage(Group group, HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		
		try {
			if (!group.pageUrl.equals("/no/page/url/defined")){
				if (Page.get(group.pageUrl) == null){
					Page.createPage(group.pageUrl, true);
				}
				if (Page.get(group.pageUrl+"/members") == null){
					Page.createPage(group.pageUrl + "/members", false);
				}
				
				String membershipPageUrl = group.pageUrl + "/members/index.html";
				while (membershipPageUrl.startsWith("/")){
					membershipPageUrl = membershipPageUrl.substring(1);
				}
			
				List<String> allPeople = new ArrayList<String>(group.leaders);
				for (String s : group.members){
					if (!allPeople.contains(s)){
						allPeople.add(s);
					}
				}
				
				Map<String, Person> inDB = Group.ofy.load().type(Person.class).ids(allPeople);
				List<Person> people = new ArrayList<Person>();
				
				for (String s : allPeople){
					Person p = inDB.get(s);
					if (p == null){
						p = Person.get(s);
					}
					people.add(p);
				}
				
				Person p = Person.get(UserAPI.email());
				
				req.setAttribute("production", true);
				req.setAttribute("lastEditorEmail", p.email);
				req.setAttribute("lastEditorName", p.nickname);
				req.setAttribute("roles", group.roles);
				String now = (new SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' hh:mm a")).format(new Date());
				req.setAttribute("lastEditorDate", now);
				req.setAttribute("group", group);
				req.setAttribute("people", people);
				
				String pageHtml = JSPRenderServlet.render("/WEB-INF/pages/group-member-page.jsp", req, resp);
			
				String commitMessage = String.format("Membership page updated at %s.", (new Date()).toString());
				
				GithubAPI.createOrUpdateFile(membershipPageUrl, commitMessage, pageHtml);
				
				Log.info(String.format("Updated membership page for group [%s][%s] successfully", group.name, group.id));
			} else {
				Log.warn(String.format("No pageURL defined for group [%s][%s], so it was not updated.", group.id, group.name));
			}
		} catch (IOException ioe){
			Log.error(String.format("Error in updating membership page: [%s]", ioe.getMessage()));
		}
		
		
	}
	
	private static boolean isGroupLeader(Group g){
		if (g == null){
			return false;
		}
		if (UserAPI.isOwner()){
			return true;
		}
		String userEmail = UserAPI.email();
		if (userEmail != null && g.leaders.contains(userEmail)){
			return true;
		}
		return false;
	}
}
