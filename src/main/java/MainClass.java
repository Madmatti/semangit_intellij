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

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainClass implements Runnable {

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
        //ProjectCommits
        prefixTable.put(TAG_Semangit + TAG_Repoprefix, ""); //most common prefix gets empty prefix in output
        prefixTable.put(TAG_Semangit + "commit_repository", "a");
        prefixTable.put(TAG_Semangit + TAG_Commitprefix, "b");


        //CommitParents
        prefixTable.put(TAG_Semangit + "commit_has_parent", "c");

        //Followers
        prefixTable.put(TAG_Semangit + "github_commit", "d");
        prefixTable.put(TAG_Semangit + "commit_sha", "e");
        prefixTable.put(TAG_Semangit + "commit_author", "f");
        prefixTable.put(TAG_Semangit + "commit_committed_by", "g");
        prefixTable.put(TAG_Semangit + "commit_created_at", "h");
        prefixTable.put(TAG_Semangit + "github_follow_event", "i");
        prefixTable.put(TAG_Semangit + "github_following_since", "j");
        prefixTable.put(TAG_Semangit + "github_user_or_project", "k");
        prefixTable.put(TAG_Semangit + "github_follower", "l");
        prefixTable.put(TAG_Semangit  + TAG_Userprefix, "m");
        prefixTable.put(TAG_Semangit + "github_follows", "n");

        //Issue events
        prefixTable.put(TAG_Semangit + "github_issue_event", "o");
        prefixTable.put(TAG_Semangit + "github_issue_event_created_at", "p");
        prefixTable.put(TAG_Semangit + "github_issue_event_action_specific_sha", "q");
        prefixTable.put(TAG_Semangit + "github_issue_event_action", "r");
        prefixTable.put(TAG_Semangit + "github_issue_event_actor", "s");
        prefixTable.put(TAG_Semangit + "github_issue_event_for", "t");

        //Issue Labels
        prefixTable.put(TAG_Semangit + TAG_Issueprefix, "u");
        prefixTable.put(TAG_Semangit + TAG_Repolabelprefix, "v");
        prefixTable.put(TAG_Semangit + "github_issue_label_used_by", "w");

        //Issues
        prefixTable.put(TAG_Semangit + "github_issue", "x");
        prefixTable.put(TAG_Semangit + "github_issue_project", "y");
        prefixTable.put(TAG_Semangit + "github_issue_reporter", "z");
        prefixTable.put(TAG_Semangit + "github_issue_assignee", "aa");
        prefixTable.put(TAG_Semangit + "github_issue_pull_request", "ab");
        prefixTable.put(TAG_Semangit + "github_issue_created_at", "ac");
        prefixTable.put(TAG_Semangit + TAG_Pullrequestprefix, "ad");


        //Organization Members
        prefixTable.put(TAG_Semangit + "github_organization_join_event", "ae");
        prefixTable.put(TAG_Semangit + "github_organization_joined_at", "af");
        prefixTable.put(TAG_Semangit + "github_organization_joined_by", "ag");
        prefixTable.put(TAG_Semangit + "github_organization_is_joined", "ah");

        //Project Members
        prefixTable.put(TAG_Semangit + "github_project_join_event", "ai");
        prefixTable.put(TAG_Semangit + "github_project_join_event_created_at", "aj");
        prefixTable.put(TAG_Semangit + "github_project_joining_user", "ak");
        prefixTable.put(TAG_Semangit + "github_project_joined", "al");

        //Projects
        prefixTable.put(TAG_Semangit + "github_project", "am");
        prefixTable.put(TAG_Semangit + "repository_url", "an");
        prefixTable.put(TAG_Semangit + "github_has_owner", "ao");
        prefixTable.put(TAG_Semangit + "github_project_name", "ap");
        prefixTable.put(TAG_Semangit + "github_project_description", "aq");
        prefixTable.put(TAG_Semangit + "repository_language", "ar");
        prefixTable.put(TAG_Semangit + "github_forked_from", "as");
        prefixTable.put(TAG_Semangit + "github_project_deleted", "at");
        prefixTable.put(TAG_Semangit + "repository_created_at", "au");

        //Pull Request Commits
        prefixTable.put(TAG_Semangit + "pull_request_has_commit", "av");

        //Pull Request History
        prefixTable.put(TAG_Semangit + "github_pull_request_action", "aw");
        prefixTable.put(TAG_Semangit + "github_pull_request_action_created_at", "ax");
        prefixTable.put(TAG_Semangit + "github_pull_request_action_id", "ay");
        prefixTable.put(TAG_Semangit + "github_pull_request_action_type", "az");
        prefixTable.put(TAG_Semangit + "github_pull_request_actor", "ba");
        prefixTable.put(TAG_Semangit + "github_pull_request_action_pull_request", "bb");

        //Pull Requests
        prefixTable.put(TAG_Semangit + "github_pull_request", "bc");
        prefixTable.put(TAG_Semangit + "pull_request_base_project", "bd");
        prefixTable.put(TAG_Semangit + "pull_request_head_project", "be");
        prefixTable.put(TAG_Semangit + "pull_request_base_commit", "bf");
        prefixTable.put(TAG_Semangit + "pull_request_head_commit", "bg");
        prefixTable.put(TAG_Semangit + "github_pull_request_id", "bh");
        prefixTable.put(TAG_Semangit + "github_pull_request_intra_branch", "bi");

        //Repo Labels
        prefixTable.put(TAG_Semangit + "github_repo_label", "bj");
        prefixTable.put(TAG_Semangit + "github_repo_label_project", "bk");
        prefixTable.put(TAG_Semangit + "github_repo_label_name", "bl");

        //User
        prefixTable.put(TAG_Semangit + "github_user", "bm");
        prefixTable.put(TAG_Semangit + "github_login", "bn");
        prefixTable.put(TAG_Semangit + "github_name", "bo");
        prefixTable.put(TAG_Semangit + "github_company", "bp");
        prefixTable.put(TAG_Semangit + "github_user_location", "bq");
        prefixTable.put(TAG_Semangit + "user_email", "br");
        prefixTable.put(TAG_Semangit + "github_user_created_at", "bs");
        prefixTable.put(TAG_Semangit + "github_user_is_org", "bt");
        prefixTable.put(TAG_Semangit + "github_user_deleted", "bu");
        prefixTable.put(TAG_Semangit + "github_user_fake", "bv");

        //Watchers == Followers

        //Comments
        prefixTable.put(TAG_Semangit + "comment", "cd");
        prefixTable.put(TAG_Semangit + TAG_Commentprefix + "commit_", "bw");
        prefixTable.put(TAG_Semangit + "comment_for", "bx");
        prefixTable.put(TAG_Semangit + "comment_author", "by");
        prefixTable.put(TAG_Semangit + "comment_body", "bz");
        prefixTable.put(TAG_Semangit + "comment_line", "ca");
        prefixTable.put(TAG_Semangit + "comment_pos", "cb");
        prefixTable.put(TAG_Semangit + "comment_created_at", "cc");
        /*
        prefixTable.put(, "ce");
        prefixTable.put(, "cf");
        prefixTable.put(, "cg");
        prefixTable.put(, "ch");
        prefixTable.put(, "ci");
        prefixTable.put(, "cj");
        prefixTable.put(, "ck");*/

        //TODO: Repo Milestones...
    }

    private static String getPrefix(String s)
    {
        if(prefixTable.get(s) == null)
        {
            System.out.println("Prefix for " + s + " missing.");
        }
        return prefixTable.get(s) + ":";
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
                    writer.write(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[0] + " " + getPrefix(TAG_Semangit + "commit_has_parent") + " " + getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[1]);
                }
                else
                {
                    writer.write(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[1]); //only specifying next object. subject/predicate are abbreviated
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
                writer.write(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[0] + " " + getPrefix(TAG_Semangit + "commit_has_parent") + " " + getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[1] + ".");
            }
            else
            {
                writer.write(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[1] + "."); //only specifying next object. subject/predicate are abbreviated
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
                String commitURI = getPrefix(TAG_Semangit + TAG_Commitprefix) + nextLine[0];
                writer.write(  commitURI + " a " + getPrefix(TAG_Semangit + "github_commit") + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "commit_sha") + " \"" + nextLine[1] + "\";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "commit_author") + " " + getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[2] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "commit_committed_by") + " " + getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[3] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "commit_repository") + " " + getPrefix(TAG_Semangit + TAG_Repoprefix) + nextLine[4] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "commit_created_at") + " \"" + nextLine[5] + "\".");
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
                writer.write("[ a " + getPrefix(TAG_Semangit + "github_follow_event") + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_following_since") + " \"" + nextLine[2] + "\" ;");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_user_or_project") + " 0 ] " + getPrefix(TAG_Semangit + "github_follower") + " " + getPrefix(TAG_Semangit  + TAG_Userprefix) + nextLine[1] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_follows") + " " + getPrefix(TAG_Semangit  + TAG_Userprefix) + nextLine[0] + ".");
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
                writer.write("[ a " + getPrefix(TAG_Semangit + "github_issue_event") + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_issue_event_created_at") + " \"" + nextLine[5] + "\";");
                writer.newLine();
                if(!nextLine[4].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_issue_event_action_specific_sha") + " \"" + nextLine[4] + "\";");
                    writer.newLine();
                }
                writer.write(getPrefix(TAG_Semangit + "github_issue_event_action") + " \"" + nextLine[3] + "\" ] ");
                writer.write(getPrefix(TAG_Semangit + "github_issue_event_actor") + " " + getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[2] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_issue_event_for") + " " + getPrefix(TAG_Semangit + TAG_Issueprefix) + nextLine[1] + ".");
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
                    writer.write(getPrefix(TAG_Semangit + TAG_Issueprefix) + curLine[1]); //only print object
                }
                else
                {
                    writer.write(getPrefix(TAG_Semangit + TAG_Repolabelprefix) + curLine[0] + " " + getPrefix(TAG_Semangit + "github_issue_label_used_by") + " " + getPrefix(TAG_Semangit + TAG_Issueprefix) + curLine[1]); //print entire triple
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
                writer.write(getPrefix(TAG_Semangit + TAG_Issueprefix) + curLine[1]); //only print object
            }
            else
            {
                writer.write(getPrefix(TAG_Semangit + TAG_Repolabelprefix) + curLine[0] + " " + getPrefix(TAG_Semangit + "github_issue_label_used_by") + " " + getPrefix(TAG_Semangit + TAG_Issueprefix) + curLine[1]); //print entire triple
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
                String issueURL = getPrefix(TAG_Semangit + TAG_Issueprefix) + curLine[7];
                writer.write( issueURL + " a " + TAG_Semangit + "github_issue;");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_issue_project") + " " + getPrefix(TAG_Semangit + TAG_Repoprefix) + curLine[1] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_issue_reporter") + " " + getPrefix(TAG_Semangit + TAG_Userprefix) + curLine[2] + ";");
                writer.newLine();
                if(!curLine[3].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_issue_assignee") + " " + getPrefix(TAG_Semangit + TAG_Userprefix) + curLine[3] + ";");
                    writer.newLine();
                }
                if(!curLine[5].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_issue_pull_request") + " " + getPrefix(TAG_Semangit + TAG_Pullrequestprefix) + curLine[5] + ";");
                    writer.newLine();
                }
                writer.write(getPrefix(TAG_Semangit + "github_issue_created_at") + " \"" + curLine[6] + "\".");
                writer.newLine();
                curLine = nextLine;

            }
            //TODO: Handle last line!!!
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
                writer.write("[ a " + getPrefix(TAG_Semangit + "github_organization_join_event") + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_organization_joined_at") + " \"" + nextLine[2] + "\" ] " + getPrefix(TAG_Semangit + "github_organization_joined_by") + " " + getPrefix(TAG_Semangit  + TAG_Userprefix) + nextLine[1] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_organization_is_joined") + " " + getPrefix(TAG_Semangit  + TAG_Userprefix) + nextLine[0] + ".");
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
                    writer.write(getPrefix(TAG_Semangit + TAG_Repoprefix) + curLine[0]); //one commit for multiple repositories (branching / merging)
                }
                else //no abbreviation occurred. Full subject predicate object triple printed
                {
                    writer.write(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[1] + " " + getPrefix(TAG_Semangit + "commit_repository") + " " + getPrefix(TAG_Semangit + TAG_Repoprefix) + curLine[0]);
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
                writer.write(getPrefix(TAG_Semangit + TAG_Repoprefix) + curLine[0] + "."); //one commit for multiple repositories (branching / merging)
            }
            else //no abbreviation occurred. Full subject predicate object triple printed
            {
                writer.write(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[1] + " " + getPrefix(TAG_Semangit + "commit_repository") + " " + getPrefix(TAG_Semangit + TAG_Repoprefix) + curLine[0] + ".");
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
                writer.write("[ a " + getPrefix(TAG_Semangit + "github_project_join_event") + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_project_join_event_created_at") + " \"" + nextLine[2] + "\" ] ");
                writer.write(getPrefix(TAG_Semangit + "github_project_joining_user") + " " + getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[1] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_project_joined") + " " + getPrefix(TAG_Semangit + TAG_Repoprefix) + nextLine[0] + ".");
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
                writer.write(getPrefix(TAG_Semangit + TAG_Repoprefix) + nextLine[0] + " a " + getPrefix(TAG_Semangit + "github_project") + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "repository_url") + " \"" + nextLine[1] + "\";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_has_owner") + " " + getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[2] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_project_name") + " \"" + nextLine[3] + "\";");
                writer.newLine();
                if(!nextLine[4].equals("")) {

                    writer.write(getPrefix(TAG_Semangit + "github_project_description") + " \"" + nextLine[4] + "\";");
                    writer.newLine();
                }
                if(!nextLine[5].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "repository_language") + " \"" + nextLine[5] + "\";"); //TODO! Programming language is not a string!
                    writer.newLine();
                }
                if(!nextLine[7].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_forked_from") + " " + getPrefix(TAG_Semangit + TAG_Repoprefix) + nextLine[7] + ";");
                    writer.newLine();
                }
                if(nextLine[8].equals("1"))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_project_deleted") + " 1;");
                    writer.newLine();
                }
                else
                {
                    writer.write(getPrefix(TAG_Semangit + "github_project_deleted") + " 0;");
                    writer.newLine();
                }
                writer.write(getPrefix(TAG_Semangit + "repository_created_at") + " \"" + nextLine[6] + "\".");
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
                    writer.write(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[1]);
                }
                else
                {
                    writer.write(getPrefix(TAG_Semangit + TAG_Pullrequestprefix) + curLine[0] + " " + getPrefix(TAG_Semangit + "pull_request_has_commit") + " " + getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[1]);
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
                writer.write(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[1] + ".");
            }
            else
            {
                writer.write(getPrefix(TAG_Semangit + TAG_Pullrequestprefix) + curLine[0] + " " + getPrefix(TAG_Semangit + "pull_request_has_commit") + " " + getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[1] + ".");
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
                writer.write("[ a " + getPrefix(TAG_Semangit + "github_pull_request_action") + " ;");
                writer.newLine();
                //id, PR id, created at, action, actor
                writer.write(getPrefix(TAG_Semangit + "github_pull_request_action_created_at") + " \"" + nextLine[2] + "\";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_pull_request_action_id") + " " + nextLine[0] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_pull_request_action_type") + " \"" + nextLine[3] + "\" ] ");
                if(!nextLine[4].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_pull_request_actor") + " " + getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[4] + ";");
                    writer.newLine();
                }
                writer.write(getPrefix(TAG_Semangit + "github_pull_request_action_pull_request") + " " + getPrefix(TAG_Semangit + TAG_Pullrequestprefix) + nextLine[1] + ".");
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
                writer.write(getPrefix(TAG_Semangit + TAG_Pullrequestprefix) + nextLine[0] + " a " + getPrefix(TAG_Semangit + "github_pull_request") + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "pull_request_base_project") + " " + getPrefix(TAG_Semangit + TAG_Repoprefix) + nextLine[2] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "pull_request_head_project") + " " + getPrefix(TAG_Semangit + TAG_Repoprefix) + nextLine[1] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "pull_request_base_commit") + " " + getPrefix(TAG_Semangit + TAG_Commitprefix) + nextLine[4] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "pull_request_head_commit") + " " + getPrefix(TAG_Semangit + TAG_Commitprefix) + nextLine[3] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_pull_request_id") + " " + nextLine[5] + ";"); //TODO: ^^xsd:int?!
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_pull_request_intra_branch") + " " + nextLine[6] + ".");
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
                writer.write(getPrefix(TAG_Semangit + TAG_Repolabelprefix) + nextLine[0] + " a " + getPrefix(TAG_Semangit + "github_repo_label") + " ;");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_repo_label_project") + " " + getPrefix(TAG_Semangit + TAG_Repoprefix) + nextLine[1] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_repo_label_name") + " \"" + nextLine[2] + "\".");
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
                String userURI = getPrefix(TAG_Semangit  + TAG_Userprefix) + nextLine[0];
                writer.write(userURI + " a " + getPrefix(TAG_Semangit + "github_user") + " ;");
                writer.newLine();
                if(!nextLine[1].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_login") + " \"" + nextLine[1] + "\";");
                    writer.newLine();
                }
                if(!nextLine[2].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_name") + " \"" + nextLine[2] + "\";");
                    writer.newLine();
                }
                if(!nextLine[3].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_company") + " \"" + nextLine[3] + "\";");
                    writer.newLine();
                }
                if(!nextLine[4].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_user_location") + " \"" + nextLine[4] + "\";");
                    writer.newLine();
                }
                if(!nextLine[5].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "user_email") + " \"" + nextLine[5] + "\";");
                    writer.newLine();
                }
                writer.write(getPrefix(TAG_Semangit + "github_user_created_at") + " \"" + nextLine[6] + "\";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_user_is_org") + " ");
                if(nextLine[7].equals("USR"))
                {
                    writer.write("false ;"); //TODO!!! Other booleans are 0 or 1, not false / true
                    writer.newLine();
                }
                else
                {
                    writer.write("true ;");
                    writer.newLine();
                }
                writer.write(getPrefix(TAG_Semangit + "github_user_deleted") + " ");
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
                writer.write(getPrefix(TAG_Semangit + "github_user_fake") + " ");
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
                writer.write("[ a " + getPrefix(TAG_Semangit + "github_follow_event") + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_following_since") + " \"" + nextLine[2] + "\" ;");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_user_or_project") + " 1 ] " + getPrefix(TAG_Semangit + "github_follower") + " " + getPrefix(TAG_Semangit  + TAG_Userprefix) + nextLine[1] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_follows") + " " + getPrefix(TAG_Semangit  + TAG_Repoprefix) + nextLine[0] + ".");
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
                writer.write(getPrefix(TAG_Semangit + TAG_Commentprefix + "commit_") + nextLine[0] + " a " + getPrefix(TAG_Semangit + "comment") + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "comment_for") + " " + getPrefix(TAG_Semangit + TAG_Commitprefix) + nextLine[1] + ";"); //comment for a commit
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "comment_author") + " " + getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[2] + ";");
                writer.newLine();
                if(!nextLine[3].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "comment_body") + " \"" + nextLine[3] + "\";");
                    writer.newLine();
                }

                if(!nextLine[4].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "comment_line") + " " + nextLine[4] + ";");
                    writer.newLine();
                }

                if(!nextLine[5].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "comment_pos") + " " + nextLine[5] + ";");
                    writer.newLine();
                }


                writer.write(getPrefix(TAG_Semangit + "comment_created_at") + " \"" + nextLine[7] + "\".");
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
                writer.write("[" + getPrefix(TAG_Semangit + "comment_created_at") + " \"" + nextLine[3] + "\";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "comment_for") + " " + getPrefix(TAG_Semangit + TAG_Issueprefix) + nextLine[0] + ";"); //comment for an issue
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "comment_author") + " " + getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[1] + "] a " + getPrefix(TAG_Semangit + "comment") + ".");
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
                writer.write("[" + getPrefix(TAG_Semangit + "comment_created_at") + " \"" + nextLine[6] + "\";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "comment_for") + " " + getPrefix(TAG_Semangit + TAG_Pullrequestprefix) + nextLine[0] + ","); //comment for a pull request
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + TAG_Commitprefix) + nextLine[5] + ";");
                writer.newLine();

                writer.write(getPrefix(TAG_Semangit + "comment_pos") + " " + nextLine[3] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "comment_body") + " \"" + nextLine[4] + "\";");
                writer.newLine();


                writer.write(getPrefix(TAG_Semangit + "comment_author") + " " + getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[1] + "] a " + getPrefix(TAG_Semangit + "comment") + ".");
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


    private static void appendFileToOutput(String directory, String fileName)
    {
        String outPath = directory.concat("combined.ttl");
        File index = new File(outPath);
        if(!index.exists())
        {
//            System.out.println("Index doesnt exist");
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outPath), 32768);
                final Set<Map.Entry<String, String>> entries = prefixTable.entrySet();
                for(Map.Entry<String, String> entry : entries)
                {
//                    System.out.println();
                    writer.write("@prefix " + entry.getValue() + ": <http://semangit.com/ontology/" + entry.getKey() + "#>.");
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

    MainClass(String workOnFile, String path, boolean prefixes)
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
        try {
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

            initPrefixTable();

            ArrayList<Thread> processes = new ArrayList<>();
            processes.add(new Thread(new MainClass("commit_comments", args[0], true)));
            processes.add(new Thread(new MainClass("commit_parents", args[0], false)));
            processes.add(new Thread(new MainClass("commits", args[0], false)));
            processes.add(new Thread(new MainClass("followers", args[0], false)));
            processes.add(new Thread(new MainClass("issue_comments", args[0], false)));
            processes.add(new Thread(new MainClass("issue_events", args[0], false)));
            processes.add(new Thread(new MainClass("issue_labels", args[0], false)));
            processes.add(new Thread(new MainClass("issues", args[0], false)));
            processes.add(new Thread(new MainClass("organization_members", args[0], false)));
            processes.add(new Thread(new MainClass("project_commits", args[0], false)));
            processes.add(new Thread(new MainClass("project_members", args[0], false)));
            processes.add(new Thread(new MainClass("projects", args[0], false)));
            processes.add(new Thread(new MainClass("pull_request_comments", args[0], false)));
            processes.add(new Thread(new MainClass("pull_request_commits", args[0], false)));
            processes.add(new Thread(new MainClass("pull_request_history", args[0], false)));
            processes.add(new Thread(new MainClass("pull_requests", args[0], false)));
            processes.add(new Thread(new MainClass("users", args[0], false)));
            processes.add(new Thread(new MainClass("repo_labels", args[0], false)));
            processes.add(new Thread(new MainClass("repo_milestones", args[0], false)));
            processes.add(new Thread(new MainClass("watchers", args[0], false)));

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


            String correctPath = args[0].concat("rdf/");
            appendFileToOutput(correctPath, "commit_comments.ttl");
            appendFileToOutput(correctPath, "commits.ttl");
            appendFileToOutput(correctPath, "commit_parents.ttl");
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
            appendFileToOutput(correctPath, "organization_members.ttl");
            appendFileToOutput(correctPath, "followers.ttl");
            appendFileToOutput(correctPath, "users.ttl");
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
