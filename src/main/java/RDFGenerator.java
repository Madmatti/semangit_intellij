import com.opencsv.CSVReader;


/*
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.hdt.*;
import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.apache.commons.lang3.StringEscapeUtils;
import org.rdfhdt.hdt.rdf.RDFParserCallback;
import org.rdfhdt.hdt.rdf.RDFParserFactory;
import org.rdfhdt.hdt.tools.RDF2HDT;
*/

import javax.management.relation.RoleUnresolved;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RDFGenerator implements Runnable {

    private String workOnFile;
    private String path;
    private boolean prefixes;

    private static final String PREFIX_Semangit = "<http://www.sg.com/ont/>";
    private static final String TAG_Semangit = "semangit:";
    private static final String TAG_Userprefix = "ghuser_";
    private static final String TAG_Repoprefix = "ghrepo_";
    private static final String TAG_Commitprefix = "ghcom_";
    private static final String TAG_Commentprefix = "ghcomment_";
    private static final String TAG_Issueprefix = "ghissue_";
    private static final String TAG_Pullrequestprefix = "ghpr_";
    private static final String TAG_Repolabelprefix = "ghlb_";

    private static final Map<String, String> prefixTable = new HashMap<>();
    private static void initPrefixTable()
    {
        prefixTable.put("", "");
    }

    private static void parseCommitParents(String path, boolean includePrefix) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "commit_parents.csv"));
            //TODO: Split file and sort?
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/commit_parents.ttl"), 32768);
            String[] nextLine;
            String[] curLine;
            if (includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            curLine = reader.readNext();
            boolean abbreviated = false;
            while ((nextLine = reader.readNext()) != null) {
                if(!abbreviated)
                {
                    writer.write(TAG_Semangit + TAG_Commitprefix + curLine[0] + " " + TAG_Semangit + "commit_has_parent " + TAG_Semangit + TAG_Commitprefix + curLine[1]);
                }
                else
                {
                    writer.write(TAG_Semangit + TAG_Commitprefix + curLine[1]); //only specifying next object. subject/predicate are abbreviated
                }
                if(curLine[0].equals(nextLine[0]))
                {
                    writer.write(","); //abbreviating subject and predicate for next line
                    abbreviated = true;
                }
                else
                {
                    writer.write("."); //cannot use turtle abbreviation here
                    abbreviated = false;
                }
                writer.newLine();
                curLine = nextLine;
            }
            //handle last line of file
            if(!abbreviated)
            {
                writer.write(TAG_Semangit + TAG_Commitprefix + curLine[0] + " " + TAG_Semangit + "commit_has_parent " + TAG_Semangit + TAG_Commitprefix + curLine[1] + ".");
            }
            else
            {
                writer.write(TAG_Semangit + TAG_Commitprefix + curLine[1] + "."); //only specifying next object. subject/predicate are abbreviated
            }
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void parseCommits(String path, boolean includePrefix) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "commits.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/commits.ttl"), 32768);
            String[] nextLine;
            if (includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            while ((nextLine = reader.readNext()) != null) {
                /*for (int i = 0; i < nextLine.length; i++) {
                    nextLine[i] = groovy.json.StringEscapeUtils.escapeJava(nextLine[i]);
                }*/
                String commitURI = TAG_Semangit + TAG_Commitprefix + nextLine[0];
                writer.write(  commitURI + " a " + TAG_Semangit + "github_commit;");
                writer.newLine();
                writer.write(TAG_Semangit + "commit_sha \"" + nextLine[1] + "\";");
                writer.newLine();
                writer.write(TAG_Semangit + "commit_author " + TAG_Semangit + TAG_Userprefix + nextLine[2] + ";");
                writer.newLine();
                writer.write(TAG_Semangit + "commit_committed_by " + TAG_Semangit + TAG_Userprefix + nextLine[3] + ";");
                writer.newLine();
                writer.write(TAG_Semangit + "commit_repository " + TAG_Semangit + TAG_Repoprefix + nextLine[4] + ";");
                writer.newLine();
                writer.write(TAG_Semangit + "commit_created_at \"" + nextLine[5] + "\".");
                writer.newLine();
            }
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }


    private static void parseFollowers(String path, boolean includePrefix)
    {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "followers.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/followers.ttl"), 32768);
            String[] nextLine;
            if(includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            while((nextLine = reader.readNext())!= null) {
                writer.write("[ a " + TAG_Semangit + "github_follow_event;");
                writer.newLine();
                writer.write(TAG_Semangit + "github_following_since \"" + nextLine[2] + "\" ;");
                writer.newLine();
                writer.write(TAG_Semangit + "github_user_or_project 0 ] " + TAG_Semangit + "github_follower " + TAG_Semangit  + TAG_Userprefix + nextLine[1] + ";");
                writer.newLine();
                writer.write(TAG_Semangit + "github_follows " + TAG_Semangit  + TAG_Userprefix + nextLine[0] + ".");
                writer.newLine();
            }
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }



    private static void parseIssueEvents(String path, boolean includePrefix) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "issue_events.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/issue_events.ttl"), 32768);
            String[] nextLine;
            if (includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            while ((nextLine = reader.readNext()) != null) {
                //event id, issue id, actor id, action, action specific sha, created at
                writer.write("[ a " + TAG_Semangit + "github_issue_event;");
                writer.newLine();
                writer.write(TAG_Semangit + "github_issue_event_created_at \"" + nextLine[5] + "\";");
                writer.newLine();
                if(!nextLine[4].equals("N"))
                {
                    writer.write(TAG_Semangit + "github_issue_event_action_specific_sha \"" + nextLine[4] + "\";");
                    writer.newLine();
                }
                writer.write(TAG_Semangit + "github_issue_event_action \"" + nextLine[3] + "\" ] ");
                writer.write(TAG_Semangit + "github_issue_event_actor " + TAG_Semangit + TAG_Userprefix + nextLine[2] + ";");
                writer.newLine();
                writer.write(TAG_Semangit + "github_issue_event_for " + TAG_Semangit + TAG_Issueprefix + nextLine[1] + ".");
                writer.newLine();
            }
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }



    private static void parseIssueLabels(String path, boolean includePrefix) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "issue_labels.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/issue_labels.ttl"), 32768);
            String[] nextLine;
            if (includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            String[] curLine = reader.readNext();
            boolean abbreviated = false;
            while ((nextLine = reader.readNext()) != null) {
                if(abbreviated)
                {
                    writer.write(TAG_Semangit + TAG_Issueprefix + curLine[1]); //only print object
                }
                else
                {
                    writer.write(TAG_Semangit + TAG_Repolabelprefix + curLine[0] + " " + TAG_Semangit + "github_issue_label_used_by " + TAG_Semangit + TAG_Issueprefix + curLine[1]); //print entire triple
                }
                if(curLine[0].equals(nextLine[0]))
                {
                    abbreviated = true;
                    writer.write(",");
                }
                else {
                    abbreviated = false;
                    writer.write(".");
                }
                writer.newLine();

                curLine = nextLine;
            }
            if(abbreviated)
            {
                writer.write(TAG_Semangit + TAG_Issueprefix + curLine[1]); //only print object
            }
            else
            {
                writer.write(TAG_Semangit + TAG_Repolabelprefix + curLine[0] + " " + TAG_Semangit + "github_issue_label_used_by " + TAG_Semangit + TAG_Issueprefix + curLine[1]); //print entire triple
            }
            writer.write(".");
            writer.newLine();
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }


    private static void parseIssues(String path, boolean includePrefix) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "issues.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/issues.ttl"), 32768);
            String[] nextLine;
            if (includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            String[] curLine = reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                //SCHEMA WRONG!
                //Format: id, repo id, reporter id, assignee id, pull request (0/1), pull request id, created at, issue id
                //WARNING: Duplicates! Everything except for id might be equal?! Example: issue id 1575
                if(nextLine[7].equals(curLine[7]))
                {
                    curLine = nextLine;
                    continue;
                }
                String issueURL = TAG_Semangit + TAG_Issueprefix + curLine[7];
                writer.write( issueURL + " a " + TAG_Semangit + "github_issue;");
                writer.newLine();
                writer.write(TAG_Semangit + "github_issue_project " + TAG_Semangit + TAG_Repoprefix + curLine[1] + ";");
                writer.newLine();
                writer.write(TAG_Semangit + "github_issue_reporter " + TAG_Semangit + TAG_Userprefix + curLine[2] + ";");
                writer.newLine();
                if(!curLine[3].equals("N"))
                {
                    writer.write(TAG_Semangit + "github_issue_assignee " + TAG_Semangit + TAG_Userprefix + curLine[3] + ";");
                    writer.newLine();
                }
                if(!curLine[5].equals("N"))
                {
                    writer.write(TAG_Semangit + "github_issue_pull_request " + TAG_Semangit + TAG_Pullrequestprefix + curLine[5] + ";");
                    writer.newLine();
                }
                writer.write(TAG_Semangit + "github_issue_created_at \"" + curLine[6] + "\".");
                writer.newLine();
                curLine = nextLine;

            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);

        }
    }

    private static void parseOrganizationMembers(String path, boolean includePrefix)
    {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "organization_members.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/organization_members.ttl"), 32768);
            String[] nextLine;
            if(includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            while((nextLine = reader.readNext())!= null) {
                writer.write("[ a " + TAG_Semangit + "github_organization_join_event;");
                writer.newLine();
                writer.write(TAG_Semangit + "github_organization_joined_at \"" + nextLine[2] + "\" ] " + TAG_Semangit + "github_organization_joined_by " + TAG_Semangit  + TAG_Userprefix + nextLine[1] + ";");
                writer.newLine();
                writer.write(TAG_Semangit + "github_organization_is_joined " + TAG_Semangit  + TAG_Userprefix + nextLine[0] + ".");
                writer.newLine();
            }
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }



    private static void parseProjectCommits(String path, boolean includePrefix)
    {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "project_commits.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/project_commits.ttl"), 32768);
            String[] nextLine;
            if(includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            String[] curLine = reader.readNext();
            boolean abbreviated = false;
            while((nextLine = reader.readNext())!= null) { //TODO: sticking to ontology for now. Check if reversing the relation would save space!
                if(abbreviated) //abbreviated in previous step. Only need to print object now
                {
                    writer.write(TAG_Semangit + TAG_Repoprefix + curLine[0]); //one commit for multiple repositories (branching / merging)
                }
                else //no abbreviation occurred. Full subject predicate object triple printed
                {
                    writer.write(TAG_Semangit + TAG_Commitprefix + curLine[1] + " " + TAG_Semangit + "commit_repository " + TAG_Semangit + TAG_Repoprefix + curLine[0]);
                }

                abbreviated = (curLine[1].equals(nextLine[1]));
                curLine = nextLine;
                if(abbreviated)
                {
                    writer.write(",");
                }
                else {
                    writer.write(".");
                }
                writer.newLine();
            }

            //handle last line
            if(abbreviated) //abbreviated in previous step. Only need to print object now
            {
                writer.write(TAG_Semangit + TAG_Repoprefix + curLine[0] + "."); //one commit for multiple repositories (branching / merging)
            }
            else //no abbreviation occurred. Full subject predicate object triple printed
            {
                writer.write(TAG_Semangit + TAG_Commitprefix + curLine[1] + " " + TAG_Semangit + "commit_repository " + TAG_Semangit + TAG_Repoprefix + curLine[0] + ".");
            }

            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }




    private static void parseProjectMembers(String path, boolean includePrefix)
    {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "project_members.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/project_members.ttl"), 32768);
            String[] nextLine;
            if(includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            while((nextLine = reader.readNext())!= null) {
                writer.write("[ a " + TAG_Semangit + "github_project_join_event;");
                writer.newLine();
                writer.write(TAG_Semangit + "github_project_join_event_created_at \"" + nextLine[2] + "\" ] ");
                writer.write(TAG_Semangit + "github_project_joining_user " + TAG_Semangit + TAG_Userprefix + nextLine[1] + ";");
                writer.newLine();
                writer.write(TAG_Semangit + "github_project_joined " + TAG_Semangit + TAG_Repoprefix + nextLine[0] + ".");
                writer.newLine();
            }
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }




    private static void parseProjects(String path, boolean includePrefix)
    {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "projects.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/projects.ttl"), 32768);
            String[] nextLine;
            if(includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            while((nextLine = reader.readNext())!= null) {
                for (int i = 0; i < nextLine.length; i++) {
                    nextLine[i] = groovy.json.StringEscapeUtils.escapeJava(nextLine[i]);
                }
                writer.write(TAG_Semangit + TAG_Repoprefix + nextLine[0] + " a " + TAG_Semangit + "github_project ;");
                writer.newLine();
                writer.write(TAG_Semangit + "repository_url \"" + nextLine[1] + "\";");
                writer.newLine();
                writer.write(TAG_Semangit + "github_has_owner " + TAG_Semangit + TAG_Userprefix + nextLine[2] + ";");
                writer.newLine();
                writer.write(TAG_Semangit + "github_project_name \"" + nextLine[3] + "\";");
                writer.newLine();
                if(!nextLine[4].equals("")) {

                    writer.write(TAG_Semangit + "github_project_description \"" + nextLine[4] + "\";");
                    writer.newLine();
                }
                if(!nextLine[5].equals("N"))
                {
                    writer.write(TAG_Semangit + "repository_language \"" + nextLine[5] + "\";"); //TODO! Programming language is not a string!
                    writer.newLine();
                }
                if(!nextLine[7].equals("N"))
                {
                    writer.write(TAG_Semangit + "github_forked_from " + TAG_Semangit + TAG_Repoprefix + nextLine[7] + ";");
                    writer.newLine();
                }
                if(nextLine[8].equals("1"))
                {
                    writer.write(TAG_Semangit + "github_project_deleted 1;");
                    writer.newLine();
                }
                else
                {
                    writer.write(TAG_Semangit + "github_project_deleted 0;");
                    writer.newLine();
                }
                writer.write(TAG_Semangit + "repository_created_at \"" + nextLine[6] + "\".");
                writer.newLine();
            }
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }


    private static void parsePullRequestCommits(String path, boolean includePrefix)
    {
        try
        {
            CSVReader reader = new CSVReader(new FileReader(path + "pull_request_commits.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/pull_request_commits.ttl"), 32768);
            String[] nextLine;
            if (includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            String[] curLine = reader.readNext();
            boolean abbreviated = false;
            while ((nextLine = reader.readNext()) != null) {
                if(abbreviated)
                {
                    writer.write(TAG_Semangit + TAG_Commitprefix + curLine[1]);
                }
                else
                {
                    writer.write(TAG_Semangit + TAG_Pullrequestprefix + curLine[0] + " " + TAG_Semangit + "pull_request_has_commit " + TAG_Semangit + TAG_Commitprefix + curLine[1]);
                }
                if(curLine[0].equals(nextLine[0]))
                {
                    abbreviated = true;
                    writer.write(",");
                }
                else
                {
                    abbreviated = false;
                    writer.write(".");
                }
                writer.newLine();
                curLine = nextLine;
            }
            //handle last line of file
            if(abbreviated)
            {
                writer.write(TAG_Semangit + TAG_Commitprefix + curLine[1] + ".");
            }
            else
            {
                writer.write(TAG_Semangit + TAG_Pullrequestprefix + curLine[0] + " " + TAG_Semangit + "pull_request_has_commit " + TAG_Semangit + TAG_Commitprefix + curLine[1] + ".");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    private static void parsePullRequestHistory(String path, boolean includePrefix) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "pull_request_history.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/pull_request_history.ttl"), 32768);
            String[] nextLine;
            if (includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            while ((nextLine = reader.readNext()) != null) {
                writer.write("[ a " + TAG_Semangit + "github_pull_request_action ;");
                writer.newLine();
                //id, PR id, created at, action, actor
                writer.write(TAG_Semangit + "github_pull_request_action_created_at \"" + nextLine[2] + "\";");
                writer.newLine();
                writer.write(TAG_Semangit + "github_pull_request_action_id " + nextLine[0] + ";");
                writer.newLine();
                writer.write(TAG_Semangit + "github_pull_request_action_type \"" + nextLine[3] + "\" ] ");
                if(!nextLine[4].equals("N"))
                {
                    writer.write(TAG_Semangit + "github_pull_request_actor " + TAG_Semangit + TAG_Userprefix + nextLine[4] + ";");
                    writer.newLine();
                }
                writer.write(TAG_Semangit + "github_pull_request_action_pull_request " + TAG_Semangit + TAG_Pullrequestprefix + nextLine[1] + ".");
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }




    private static void parsePullRequests(String path, boolean includePrefix) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "pull_requests.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/pull_requests.ttl"), 32768);
            String[] nextLine;
            if (includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            while ((nextLine = reader.readNext()) != null) {
                writer.write(TAG_Semangit + TAG_Pullrequestprefix + nextLine[0] + " a " + TAG_Semangit + "github_pull_request;");
                writer.newLine();
                writer.write(TAG_Semangit + "pull_request_base_project " + TAG_Semangit + TAG_Repoprefix + nextLine[2] + ";");
                writer.newLine();
                writer.write(TAG_Semangit + "pull_request_head_project " + TAG_Semangit + TAG_Repoprefix + nextLine[1] + ";");
                writer.newLine();
                writer.write(TAG_Semangit + "pull_request_base_commit " + TAG_Semangit + TAG_Commitprefix + nextLine[4] + ";");
                writer.newLine();
                writer.write(TAG_Semangit + "pull_request_head_commit " + TAG_Semangit + TAG_Commitprefix + nextLine[3] + ";");
                writer.newLine();
                writer.write(TAG_Semangit + "github_pull_request_id " + nextLine[5] + ";"); //TODO: ^^xsd:int?!
                writer.newLine();
                writer.write(TAG_Semangit + "github_pull_request_intra_branch " + nextLine[6] + ".");
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void parseRepoLabels(String path, boolean includePrefix)
    {
        try
        {
            CSVReader reader = new CSVReader(new FileReader(path + "repo_labels.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/repo_labels.ttl"), 32768);
            String[] nextLine;
            if(includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            while((nextLine = reader.readNext())!= null)
            {
                for (int i = 0; i < nextLine.length; i++) {
                    nextLine[i] = groovy.json.StringEscapeUtils.escapeJava(nextLine[i]);
                }
                writer.write(TAG_Semangit + TAG_Repolabelprefix + nextLine[0] + " a " + TAG_Semangit + "github_repo_label ;");
                writer.newLine();
                writer.write(TAG_Semangit + "github_repo_label_project " + TAG_Semangit + TAG_Repoprefix + nextLine[1] + ";");
                writer.newLine();
                writer.write(TAG_Semangit + "github_repo_label_name \"" + nextLine[2] + "\".");
                writer.newLine();
            }
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }


    private static void parseRepoMilestones(String path, boolean includePrefix)
    {
        try
        {
            CSVReader reader = new CSVReader(new FileReader(path + "repo_milestones.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/repo_milestones.ttl"), 32768);
            String[] nextLine;
            if(includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            while((nextLine = reader.readNext())!= null)
            {
                for (int i = 0; i < nextLine.length; i++) {
                    nextLine[i] = groovy.json.StringEscapeUtils.escapeJava(nextLine[i]);
                }
                //TODO! repo_milestones is empty in oldest dump! Cannot convert without data...
            }
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }




    private static void parseUsers(String path, boolean includePrefix)
    {
        try
        {
            CSVReader reader = new CSVReader(new FileReader(path + "users.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/users.ttl"), 32768);
            String[] nextLine;
            if(includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            while((nextLine = reader.readNext())!= null)
            {
                for (int i = 0; i < nextLine.length; i++) {
                    nextLine[i] = groovy.json.StringEscapeUtils.escapeJava(nextLine[i]);
                }
                String userURI = TAG_Semangit  + TAG_Userprefix + nextLine[0];
                writer.write(userURI + " a " + TAG_Semangit + "github_user ;");
                writer.newLine();
                if(!nextLine[1].equals("N"))
                {
                    writer.write(TAG_Semangit + "github_login " + "\"" + nextLine[1] + "\";");
                    writer.newLine();
                }
                if(!nextLine[2].equals("N"))
                {
                    writer.write(TAG_Semangit + "github_name " + "\"" + nextLine[2] + "\";");
                    writer.newLine();
                }
                if(!nextLine[3].equals("N"))
                {
                    writer.write(TAG_Semangit + "github_company " + "\"" + nextLine[3] + "\";");
                    writer.newLine();
                }
                if(!nextLine[4].equals("N"))
                {
                    writer.write(TAG_Semangit + "github_user_location " + "\"" + nextLine[4] + "\";");
                    writer.newLine();
                }
                if(!nextLine[5].equals("N"))
                {
                    writer.write(TAG_Semangit + "user_email " + "\"" + nextLine[5] + "\";");
                    writer.newLine();
                }
                writer.write(TAG_Semangit + "github_user_created_at " + "\"" + nextLine[6] + "\";");
                writer.newLine();
                writer.write(TAG_Semangit + "github_user_is_org ");
                if(nextLine[7].equals("USR"))
                {
                    writer.write("false ;");
                    writer.newLine();
                }
                else
                {
                    writer.write("true ;");
                    writer.newLine();
                }
                writer.write(TAG_Semangit + "github_user_deleted ");
                if(nextLine[8].equals("0"))
                {
                    writer.write("false ;");
                    writer.newLine();
                }
                else
                {
                    writer.write("true ;");
                    writer.newLine();
                }
                writer.write(TAG_Semangit + "github_user_fake ");
                if(nextLine[9].equals("0"))
                {
                    writer.write("false .");
                    writer.newLine();
                }
                else
                {
                    writer.write("true .");
                    writer.newLine();
                }
            }
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    //watchers

    private static void parseWatchers(String path, boolean includePrefix)
    {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "watchers.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/watchers.ttl"), 32768);
            String[] nextLine;
            if(includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            while((nextLine = reader.readNext())!= null) {
                writer.write("[ a " + TAG_Semangit + "github_follow_event;");
                writer.newLine();
                writer.write(TAG_Semangit + "github_following_since \"" + nextLine[2] + "\" ;");
                writer.newLine();
                writer.write(TAG_Semangit + "github_user_or_project 1 ] " + TAG_Semangit + "github_follower " + TAG_Semangit  + TAG_Userprefix + nextLine[1] + ";");
                writer.newLine();
                writer.write(TAG_Semangit + "github_follows " + TAG_Semangit  + TAG_Repoprefix + nextLine[0] + ".");
                writer.newLine();
            }
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }





    /**
     * Comment section. Below are all functions related to comments.
     * commit_comments
     * issue_comments
     * pull_request_comments
     */

    private static void parseCommitComments(String path, boolean includePrefix) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "commit_comments.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/commit_comments.ttl"), 32768);
            String[] nextLine;
            if (includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            while ((nextLine = reader.readNext()) != null) {
                for (int i = 0; i < nextLine.length; i++) {
                    nextLine[i] = groovy.json.StringEscapeUtils.escapeJava(nextLine[i]);
                }
                writer.write(TAG_Semangit + TAG_Commentprefix + "commit_" + nextLine[0] + " a " + TAG_Semangit + "comment;");
                writer.newLine();
                writer.write(TAG_Semangit + "comment_for " + TAG_Semangit + TAG_Commitprefix + nextLine[1] + ";"); //comment for a commit
                writer.newLine();
                writer.write(TAG_Semangit + "comment_author " + TAG_Semangit + TAG_Userprefix + nextLine[2] + ";");
                writer.newLine();
                if(!nextLine[3].equals("N"))
                {
                    writer.write(TAG_Semangit + "comment_body \"" + nextLine[3] + "\";");
                    writer.newLine();
                }

                if(!nextLine[4].equals("N"))
                {
                    writer.write(TAG_Semangit + "comment_line " + nextLine[4] + ";");
                    writer.newLine();
                }

                if(!nextLine[5].equals("N"))
                {
                    writer.write(TAG_Semangit + "comment_pos " + nextLine[5] + ";");
                    writer.newLine();
                }


                writer.write(TAG_Semangit + "comment_created_at \"" + nextLine[7] + "\".");
                writer.newLine();
            }
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }


    private static void parseIssueComments(String path, boolean includePrefix) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "issue_comments.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/issue_comments.ttl"), 32768);
            String[] nextLine;
            if (includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            while ((nextLine = reader.readNext()) != null) {
                /*for (int i = 0; i < nextLine.length; i++) {
                    nextLine[i] = groovy.json.StringEscapeUtils.escapeJava(nextLine[i]);
                }*/

                //TODO: Let's verify the integrity of the RDF output of this
                writer.write("[" + TAG_Semangit + "comment_created_at \"" + nextLine[3] + "\";");
                writer.newLine();
                writer.write(TAG_Semangit + "comment_for " + TAG_Semangit + TAG_Issueprefix + nextLine[0] + ";"); //comment for an issue
                writer.newLine();
                writer.write(TAG_Semangit + "comment_author " + TAG_Semangit + TAG_Userprefix + nextLine[1] + "] a " + TAG_Semangit + "comment.");
                writer.newLine();
            }
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }



    private static void parsePullRequestComments(String path, boolean includePrefix) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "pull_request_comments.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/pull_request_comments.ttl"), 32768);
            String[] nextLine;
            if (includePrefix) {
                writer.write("@prefix semangit: " + PREFIX_Semangit + " .");
                writer.newLine();
                writer.newLine();
            }
            while ((nextLine = reader.readNext()) != null) {
                for (int i = 0; i < nextLine.length; i++) {
                    nextLine[i] = groovy.json.StringEscapeUtils.escapeJava(nextLine[i]);
                }

                //TODO: Let's verify the integrity of the RDF output of this
                writer.write("[" + TAG_Semangit + "comment_created_at \"" + nextLine[6] + "\";");
                writer.newLine();
                writer.write(TAG_Semangit + "comment_for " + TAG_Semangit + TAG_Pullrequestprefix + nextLine[0] + ","); //comment for a pull request
                writer.newLine();
                writer.write(TAG_Semangit + TAG_Commitprefix + nextLine[5] + ";");
                writer.newLine();

                writer.write(TAG_Semangit + "comment_pos " + nextLine[3] + ";");
                writer.newLine();
                writer.write(TAG_Semangit + "comment_body \"" + nextLine[4] + "\";");
                writer.newLine();


                writer.write(TAG_Semangit + "comment_author " + TAG_Semangit + TAG_Userprefix + nextLine[1] + "] a " + TAG_Semangit + "comment.");
                writer.newLine();
                //comment for [0]
            }
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Files still to be converted:
     *     repo_milestones //MISSING IN DUMP!
     */




    //removed! HDT fails to satisfy our needs, as there are too many bugs in the software, making it impossible to merge very large files
    /*public static void rdf2hdt(String path, String table)
    {
        String baseURI = "http://example.com/mydataset";
        String rdfInput = path.concat("rdf/" + table + ".ttl");
        String inputType = "turtle";    commit_comments
    commit_parents
    commits
    issue_comments
    issue_events


        String hdtOutput = path.concat("hdt/" + table + ".hdt");
        try
        {
            //HDTSpecification specification = new HDTSpecification();
            //specification.set("triples.format", HDTVocabulary.TRIPLES_TYPE_TRIPLESLIST);
            HDT hdt = HDTManager.generateHDT(rdfInput, baseURI, RDFNotation.parse(inputType), new HDTSpecification(), null);
            hdt.saveToHDT(hdtOutput, null);
            hdt.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }*/

    private static void appendFileToOutput(String directory, String fileName)
    {

        String outPath = directory.concat("combined.ttl");
        try(BufferedReader br = new BufferedReader(new FileReader(directory.concat(fileName)))) {
            Writer output = new BufferedWriter(new FileWriter(outPath, true));
            for(String line; (line = br.readLine()) != null; ) {
                output.append(line);
                ((BufferedWriter) output).newLine();
            }
            output.close();
            // line is not visible here.
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    RDFGenerator(String workOnFile, String path, boolean prefixes)
    {
        this.workOnFile = workOnFile;
        this.path = path;
        this.prefixes = prefixes;
    }

    public void run()
    {
        if(this.workOnFile == null)
        {
            throw(new RuntimeException("You need to define a file to work on!"));
        }
        switch ( workOnFile )
        {
            case "commit_comments": parseCommitComments(this.path, this.prefixes);break;
            case "commit_parents": parseCommitParents(this.path, this.prefixes);break;
            case "commits": parseCommits(this.path, this.prefixes);break;
            case "followers": parseFollowers(this.path, this.prefixes);break;
            case "issue_comments": parseIssueComments(this.path, this.prefixes);break;
            case "issue_events": parseIssueEvents(this.path, this.prefixes);break;
            case "issue_labels": parseIssueLabels(this.path, this.prefixes);break;
            case "issues": parseIssues(this.path, this.prefixes);break;
            case "organization_members": parseOrganizationMembers(this.path, this.prefixes);break;
            case "project_commits": parseProjectCommits(this.path, this.prefixes);break;
            case "project_members": parseProjectMembers(this.path, this.prefixes);break;
            case "projects": parseProjects(this.path, this.prefixes);break;
            case "pull_request_comments": parsePullRequestComments(this.path, this.prefixes);break;
            case "pull_request_commits": parsePullRequestCommits(this.path, this.prefixes);break;
            case "pull_request_history": parsePullRequestHistory(this.path, this.prefixes);break;
            case "pull_requests": parsePullRequests(this.path, this.prefixes);break;
            case "users": parseUsers(this.path, this.prefixes);break;
            case "repo_labels": parseRepoLabels(this.path, this.prefixes);break;
            case "repo_milestones":parseRepoMilestones(this.path, this.prefixes);break;
            case "watchers":parseWatchers(this.path, this.prefixes);break;
            default: throw new RuntimeException("Unknown file name specified! Which file to parse?!");
        }

    }

    public static void main(String[] args)
    {
        File index = new File(args[0] + "rdf");
        if(index.exists())
        {
            System.out.println("rdf/ already exists. Deleting!");
            String[] entries = index.list();
            if(entries != null)
            {
                for(String s : entries)
                {
                    File currentFile = new File(index.getPath(), s);
                    if(!currentFile.delete())
                    {
                        System.out.println("Failed to delete existing file: " + index.getPath() + s);
                        System.exit(1);
                    }
                }
            }
            if(!index.delete())
            {
                System.out.println("Unable to delete rdf/ directory after deleting all entries.");
                System.exit(1);
            }
        }
        if(!index.mkdirs())
        {
            System.out.println("Unable to create " + args[0] + "rdf/ directory. Exiting.");
            System.exit(1);
        }

        //(new Thread(new RDFGenerator("organization_members", args[0], true)));


        ArrayList<Thread> processes = new ArrayList<>();
        processes.add(new Thread(new RDFGenerator("commit_comments", args[0], true)));
        processes.add(new Thread(new RDFGenerator("commit_parents", args[0], false)));
        processes.add(new Thread(new RDFGenerator("commits", args[0], false)));
        processes.add(new Thread(new RDFGenerator("followers", args[0], false)));
        processes.add(new Thread(new RDFGenerator("issue_comments", args[0], false)));
        processes.add(new Thread(new RDFGenerator("issue_events", args[0], false)));
        processes.add(new Thread(new RDFGenerator("issue_labels", args[0], false)));
        processes.add(new Thread(new RDFGenerator("issues", args[0], false)));
        processes.add(new Thread(new RDFGenerator("organization_members", args[0], false)));
        processes.add(new Thread(new RDFGenerator("project_commits", args[0], false)));
        processes.add(new Thread(new RDFGenerator("project_members", args[0], false)));
        processes.add(new Thread(new RDFGenerator("projects", args[0], false)));
        processes.add(new Thread(new RDFGenerator("pull_request_comments", args[0], false)));
        processes.add(new Thread(new RDFGenerator("pull_request_commits", args[0], false)));
        processes.add(new Thread(new RDFGenerator("pull_request_history", args[0], false)));
        processes.add(new Thread(new RDFGenerator("pull_requests", args[0], false)));
        processes.add(new Thread(new RDFGenerator("users", args[0], false)));
        processes.add(new Thread(new RDFGenerator("repo_labels", args[0], false)));
        processes.add(new Thread(new RDFGenerator("repo_milestones", args[0], false)));
        processes.add(new Thread(new RDFGenerator("watchers", args[0], false)));

        for(Thread p: processes)
        {
            p.start();
        }
        for(Thread p: processes)
        {
            try {
                p.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                System.exit(1);
            }
        }

        /*parseOrganizationMembers(args[0], true);
        //rdf2hdt(args[0], "organization_members");
        System.out.println("Organizations parsed.");

        parseFollowers(args[0], false);
        System.out.println("Followers parsed.");

        parseUsers(args[0], false);
        System.out.println("Users parsed.");

        parseCommits(args[0], false);
        System.out.println("Commits parsed.");

        parseCommitParents(args[0], false);
        System.out.println("CommitParents parsed.");

        parseCommitComments(args[0], false);
        System.out.println("CommitComments parsed.");

        parseIssueComments(args[0], false);
        System.out.println("IssueComments parsed.");

        parsePullRequestComments(args[0], false);
        System.out.println("PullRequestComments parsed.");

        parseIssueEvents(args[0], false);
        System.out.println("IssueEvents parsed.");

        parseIssues(args[0], false);
        System.out.println("Issues parsed.");

        parseProjectCommits(args[0], false);
        System.out.println("ProjectCommits parsed.");

        parseProjectMembers(args[0], false);
        System.out.println("ProjectMembers parsed.");

        parseProjects(args[0], false);
        System.out.println("Projects parsed.");

        parsePullRequestCommits(args[0], false);
        System.out.println("PullRequestCommits parsed.");

        parsePullRequestHistory(args[0], false);
        System.out.println("PullRequestHistory parsed.");

        parsePullRequests(args[0], false);
        System.out.println("PullRequests parsed.");

        parseRepoLabels(args[0], false);
        System.out.println("RepoLabels parsed.");

        parseRepoMilestones(args[0], false); //TODO: TO BE DONE!
        System.out.println("RepoMilestones parsed.");

        parseIssueLabels(args[0], false);
        System.out.println("IssueLabels parsed.");

        parseWatchers(args[0], false);
        System.out.println("Watchers parsed.");
        */
        try {
            String correctPath = args[0].concat("rdf/");
            appendFileToOutput(correctPath, "organization_members.ttl");

            appendFileToOutput(correctPath, "followers.ttl");
            appendFileToOutput(correctPath, "users.ttl");
            appendFileToOutput(correctPath, "commits.ttl");
            appendFileToOutput(correctPath, "commit_parents.ttl");
            appendFileToOutput(correctPath, "commit_comments.ttl");
            appendFileToOutput(correctPath, "issue_comments.ttl");
            appendFileToOutput(correctPath, "pull_request_comments.ttl");
            appendFileToOutput(correctPath, "issue_events.ttl");
            appendFileToOutput(correctPath, "issues.ttl");
            appendFileToOutput(correctPath, "project_members.ttl");
            appendFileToOutput(correctPath, "projects.ttl");
            appendFileToOutput(correctPath, "pull_request_history.ttl");
            appendFileToOutput(correctPath, "pull_requests.ttl");
            appendFileToOutput(correctPath, "repo_labels.ttl");
            appendFileToOutput(correctPath, "repo_milestones.ttl");
            appendFileToOutput(correctPath, "issue_labels.ttl");
            appendFileToOutput(correctPath, "watchers.ttl");

            if(index.exists())
            {
                String[] entries = index.list();
                if(entries != null)
                {
                    for(String s : entries)
                    {
                        File currentFile = new File(index.getPath(), s);
                        if(s.equals("combined.ttl"))
                        {
                            continue;
                        }
                        if(!currentFile.delete())
                        {
                            System.out.println("Failed to delete existing file: " + index.getPath() + s);
                            System.exit(1);
                        }
                    }
                }
            }


        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
