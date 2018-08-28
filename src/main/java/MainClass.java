import com.opencsv.CSVReader;

import java.io.*;
import java.util.*;

public class MainClass implements Runnable {

    private String workOnFile;
    private String path;

    //private static final String PREFIX_Semangit = "<http://www.semangit.de/ontology/>";
    private static final String TAG_Semangit = "semangit:";
    private static final String TAG_Userprefix = "ghuser_";
    private static final String TAG_Repoprefix = "ghrepo_";
    private static final String TAG_Commitprefix = "ghcom_";
    private static final String TAG_Commentprefix = "ghcomment_";
    private static final String TAG_Issueprefix = "ghissue_";
    private static final String TAG_Pullrequestprefix = "ghpr_";
    private static final String TAG_Repolabelprefix = "ghlb_";
    private static final String TAG_Langprefix = "ghlang_";

    private static int errorCtr = 0;

    private static boolean prefixing = true; //default: use prefixing
    private static int mode = 0; //default: 0 = base64, 1 = base32, 2 = base16, 3 = base10

    private static final Map<String, String> prefixTable = new HashMap<>();
    private static void initPrefixTable()
    {
        //ProjectCommits
        prefixTable.put(TAG_Semangit + TAG_Repoprefix, ""); //most common prefix gets empty prefix in output
        prefixTable.put(TAG_Semangit + "repository_has_commit", "a");
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
        prefixTable.put(TAG_Semangit + "github_issue_event_action_specific_sha", "cd");
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
        prefixTable.put(TAG_Semangit + "github_issue_assignee", "A");
        prefixTable.put(TAG_Semangit + "github_issue_pull_request", "B");
        prefixTable.put(TAG_Semangit + "github_issue_created_at", "C");
        prefixTable.put(TAG_Semangit + TAG_Pullrequestprefix, "D");


        //Organization Members
        prefixTable.put(TAG_Semangit + "github_organization_join_event", "E");
        prefixTable.put(TAG_Semangit + "github_organization_joined_at", "F");
        prefixTable.put(TAG_Semangit + "github_organization_joined_by", "G");
        prefixTable.put(TAG_Semangit + "github_organization_is_joined", "H");

        //Project Members
        prefixTable.put(TAG_Semangit + "github_project_join_event", "I");
        prefixTable.put(TAG_Semangit + "github_project_join_event_created_at", "J");
        prefixTable.put(TAG_Semangit + "github_project_joining_user", "K");
        prefixTable.put(TAG_Semangit + "github_project_joined", "L");

        //Projects
        prefixTable.put(TAG_Semangit + "github_project", "M");
        prefixTable.put(TAG_Semangit + "repository_url", "N");
        prefixTable.put(TAG_Semangit + "github_has_owner", "O");
        prefixTable.put(TAG_Semangit + "github_project_name", "P");
        prefixTable.put(TAG_Semangit + "github_project_description", "Q");
        prefixTable.put(TAG_Semangit + "repository_language", "R");
        prefixTable.put(TAG_Semangit + "github_forked_from", "S");
        prefixTable.put(TAG_Semangit + "github_project_deleted", "T");
        prefixTable.put(TAG_Semangit + "repository_created_at", "U");

        //Pull Request Commits
        prefixTable.put(TAG_Semangit + "pull_request_has_commit", "V");

        //Pull Request History
        prefixTable.put(TAG_Semangit + "github_pull_request_action", "W");
        prefixTable.put(TAG_Semangit + "github_pull_request_action_created_at", "X");
        prefixTable.put(TAG_Semangit + "github_pull_request_action_id", "Y");
        prefixTable.put(TAG_Semangit + "github_pull_request_action_type", "Z");
        prefixTable.put(TAG_Semangit + "github_pull_request_actor", "aa");
        prefixTable.put(TAG_Semangit + "github_pull_request_action_pull_request", "ab");

        //Pull Requests
        prefixTable.put(TAG_Semangit + "github_pull_request", "ac");
        prefixTable.put(TAG_Semangit + "pull_request_base_project", "ad");
        prefixTable.put(TAG_Semangit + "pull_request_head_project", "ae");
        prefixTable.put(TAG_Semangit + "pull_request_base_commit", "af");
        prefixTable.put(TAG_Semangit + "pull_request_head_commit", "ag");
        prefixTable.put(TAG_Semangit + "github_pull_request_id", "ah");
        prefixTable.put(TAG_Semangit + "github_pull_request_intra_branch", "ai");

        //Repo Labels
        prefixTable.put(TAG_Semangit + "github_repo_label", "aj");
        prefixTable.put(TAG_Semangit + "github_repo_label_project", "ak");
        prefixTable.put(TAG_Semangit + "github_repo_label_name", "al");

        //User
        prefixTable.put(TAG_Semangit + "github_user", "am");
        prefixTable.put(TAG_Semangit + "github_login", "an");
        prefixTable.put(TAG_Semangit + "github_name", "ao");
        prefixTable.put(TAG_Semangit + "github_company", "ap");
        prefixTable.put(TAG_Semangit + "github_user_location", "aq");
        prefixTable.put(TAG_Semangit + "user_email", "ar");
        prefixTable.put(TAG_Semangit + "github_user_created_at", "as");
        prefixTable.put(TAG_Semangit + "github_user_is_org", "at");
        prefixTable.put(TAG_Semangit + "github_user_deleted", "au");
        prefixTable.put(TAG_Semangit + "github_user_fake", "av");

        //Watchers == Followers

        //Comments
        prefixTable.put(TAG_Semangit + "comment", "aD");
        prefixTable.put(TAG_Semangit + TAG_Commentprefix + "commit_", "aw");
        prefixTable.put(TAG_Semangit + "comment_for", "ax");
        prefixTable.put(TAG_Semangit + "comment_author", "ay");
        prefixTable.put(TAG_Semangit + "comment_body", "az");
        prefixTable.put(TAG_Semangit + "comment_line", "aA");
        prefixTable.put(TAG_Semangit + "comment_pos", "aB");
        prefixTable.put(TAG_Semangit + "comment_created_at", "aC");


        //languages
        prefixTable.put(TAG_Semangit + TAG_Langprefix, "aD");
        prefixTable.put(TAG_Semangit + "github_project_language", "aE");
        prefixTable.put(TAG_Semangit + "github_project_language_bytes", "aF");
        prefixTable.put(TAG_Semangit + "github_project_language_timestamp", "aG");
        prefixTable.put(TAG_Semangit + "github_project_language_repo", "aH");
        prefixTable.put(TAG_Semangit + "github_project_language_is", "aI");
        prefixTable.put(TAG_Semangit + "programming_language", "aO");
        prefixTable.put(TAG_Semangit + "programming_language_name", "aP");



        //update for users
        prefixTable.put(TAG_Semangit + "github_user_lat", "aJ");
        prefixTable.put(TAG_Semangit + "github_user_lng", "aK");
        prefixTable.put(TAG_Semangit + "github_user_country_code", "aL");
        prefixTable.put(TAG_Semangit + "github_user_state", "aM");
        prefixTable.put(TAG_Semangit + "github_user_city", "aN");


        prefixTable.put(TAG_Semangit + "commit_repository", "q");

        //tag "CD" used farther up
        /*
        prefixTable.put(, "ce");
        prefixTable.put(, "cf");
        prefixTable.put(, "cg");
        prefixTable.put(, "ch");
        prefixTable.put(, "ci");
        prefixTable.put(, "cj");
        prefixTable.put(, "ck");*/

    }

    private static String getPrefix(String s)
    {
        if(prefixing) {
            if (prefixTable.get(s) == null) {
                System.out.println("Prefix for " + s + " missing.");
            }
            return prefixTable.get(s) + ":";
        }
        else {
            return s;
        }
    }

    private static String b64(String input)
    {
        if(mode == 0) {
            String alphabet64 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_"; //can only find 63 ASCII characters that work except for ":", but not sure if that breaks the syntax
            // base64 on ID only
            // for forward/backward conversion, see https://stackoverflow.com/a/26172045/9743294
            StringBuilder sb = new StringBuilder();
            try {
                String rightOfComma = input.substring(input.lastIndexOf(":") + 1);
                String leftOfComma = input.substring(0, input.lastIndexOf(":") + 1);

                int in = Integer.parseInt(rightOfComma);
                Integer j = (int) Math.ceil(Math.log(in) / Math.log(alphabet64.length()));
                for (int i = 0; i < j; i++) {
                    sb.append(alphabet64.charAt(in % alphabet64.length()));
                    in /= alphabet64.length();
                }
                return leftOfComma + sb.toString();
            } catch (Exception e) {
                errorCtr++;
                e.printStackTrace();
                return input;
            }
        }
        //base32 attempt on the ID only (not prefix)
        else if(mode == 1) {
            try {
                String rightOfComma = input.substring(input.lastIndexOf(":") + 1);
                String leftOfComma = input.substring(0, input.lastIndexOf(":") + 1);
                return leftOfComma + Integer.toString(Integer.parseInt(rightOfComma), 32);
            } catch (Exception e) {
                errorCtr++;
                e.printStackTrace();
                return input;
            }
        }

        //base36 attempt on the ID only (not prefix)
        /*String rightOfComma = input.substring(input.lastIndexOf(":") + 1);
        String leftOfComma = input.substring(0,input.lastIndexOf(":") + 1);
        return leftOfComma + Integer.toString(Integer.parseInt(rightOfComma), 36);
        */

        else if (mode == 2) {
            //base16 attempt on the ID only (not prefix)
            try {
                String rightOfComma = input.substring(input.lastIndexOf(":") + 1);
                String leftOfComma = input.substring(0, input.lastIndexOf(":") + 1);
                return leftOfComma + Integer.toString(Integer.parseInt(rightOfComma), 16);
            } catch (Exception e) {
                errorCtr++;
                e.printStackTrace();
                return input;
            }
        }
        else {
            //no conversion
            return input;
        }
    }

    private static void parseCommitParents(String path) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "commit_parents.csv"));

            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/commit_parents.ttl"), 32768);
            String[] nextLine;
            String[] curLine;

            curLine = reader.readNext();
            boolean abbreviated = false;
            while ((nextLine = reader.readNext()) != null) {

                if(nextLine.length != 2) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }

                if (!abbreviated) {
                    writer.write(b64(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[0]) + " " + getPrefix(TAG_Semangit + "commit_has_parent") + " " + b64(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[1]));
                } else {
                    writer.write(b64(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[1])); //only specifying next object. subject/predicate are abbreviated
                }
                if (curLine[0].equals(nextLine[0])) {
                    writer.write(","); //abbreviating subject and predicate for next line
                    abbreviated = true;
                } else {
                    writer.write("."); //cannot use turtle abbreviation here
                    abbreviated = false;
                }
                writer.newLine();
                curLine = nextLine;
            }
            //handle last line of file
            if(curLine.length == 2){
                if (!abbreviated) {
                    writer.write(b64(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[0]) + " " + getPrefix(TAG_Semangit + "commit_has_parent") + " " + b64(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[1]) + ".");
                } else {
                    writer.write(b64(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[1]) + "."); //only specifying next object. subject/predicate are abbreviated
                }
            }
            writer.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    
    private static void parseCommits(String path) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "commits.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/commits.ttl"), 32768);
            String[] nextLine;
            
            while ((nextLine = reader.readNext()) != null) {

                if(nextLine.length != 6) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }
                /*for (int i = 0; i < nextLine.length; i++) {
                    nextLine[i] = groovy.json.StringEscapeUtils.escapeJava(nextLine[i]);
                }*/
                String commitURI = b64(getPrefix(TAG_Semangit + TAG_Commitprefix) + nextLine[0]);
                writer.write(  commitURI + " a " + getPrefix(TAG_Semangit + "github_commit") + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "commit_sha") + " \"" + nextLine[1] + "\";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "commit_author") + " " + b64(getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[2]) + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "commit_committed_by") + " " + b64(getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[3]) + ";");
                writer.newLine();
                if(!nextLine[4].equals("N")) {
                    writer.write(getPrefix(TAG_Semangit + "commit_repository") + " " + b64(getPrefix(TAG_Semangit + TAG_Repoprefix) + nextLine[4]) + ";");
                    writer.newLine();
                }
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


    private static void parseFollowers(String path)
    {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "followers.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/followers.ttl"), 32768);
            String[] nextLine;
            
            while((nextLine = reader.readNext())!= null) {

                if(nextLine.length != 3) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }
                writer.write("[ a " + getPrefix(TAG_Semangit + "github_follow_event") + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_following_since") + " \"" + nextLine[2] + "\";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_user_or_project") + " false ] " + getPrefix(TAG_Semangit + "github_follower") + " " + b64(getPrefix(TAG_Semangit  + TAG_Userprefix) + nextLine[1]) + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_follows") + " " + b64(getPrefix(TAG_Semangit  + TAG_Userprefix) + nextLine[0]) + ".");
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



    private static void parseIssueEvents(String path) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "issue_events.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/issue_events.ttl"), 32768);
            String[] nextLine;
            
            while ((nextLine = reader.readNext()) != null) {

                if(nextLine.length != 6) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }
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
                writer.write(getPrefix(TAG_Semangit + "github_issue_event_actor") + " " + b64(getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[2]) + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_issue_event_for") + " " + b64(getPrefix(TAG_Semangit + TAG_Issueprefix) + nextLine[1]) + ".");
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



    private static void parseIssueLabels(String path) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "issue_labels.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/issue_labels.ttl"), 32768);
            String[] nextLine;
            
            String[] curLine = reader.readNext();
            boolean abbreviated = false;
            while ((nextLine = reader.readNext()) != null) {

                if(nextLine.length != 2) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }

                if(abbreviated)
                {
                    writer.write(b64(getPrefix(TAG_Semangit + TAG_Issueprefix) + curLine[1])); //only print object
                }
                else
                {
                    writer.write(b64(getPrefix(TAG_Semangit + TAG_Repolabelprefix) + curLine[0]) + " " + getPrefix(TAG_Semangit + "github_issue_label_used_by") + " " + b64(getPrefix(TAG_Semangit + TAG_Issueprefix) + curLine[1])); //print entire triple
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
                writer.write(b64(getPrefix(TAG_Semangit + TAG_Issueprefix) + curLine[1])); //only print object
            }
            else
            {
                writer.write(b64(getPrefix(TAG_Semangit + TAG_Repolabelprefix) + curLine[0]) + " " + getPrefix(TAG_Semangit + "github_issue_label_used_by") + " " + b64(getPrefix(TAG_Semangit + TAG_Issueprefix) + curLine[1])); //print entire triple
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


    private static void parseIssues(String path) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "issues.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/issues.ttl"), 32768);
            String[] nextLine;
            
            String[] curLine = reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                //SCHEMA WRONG!
                //Format: id, repo id, reporter id, assignee id, pull request (0/1), pull request id, created at, issue id
                //WARNING: Duplicates! Everything except for id might be equal?! Example: issue id 1575


                if(nextLine.length != 8) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }

                if(nextLine[7].equals(curLine[7]))
                {
                    curLine = nextLine;
                    continue;
                }
                String issueURL = b64(getPrefix(TAG_Semangit + TAG_Issueprefix) + curLine[7]);
                writer.write( issueURL + " a " + getPrefix(TAG_Semangit + "github_issue") + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_issue_project") + " " + b64(getPrefix(TAG_Semangit + TAG_Repoprefix) + curLine[1]) + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_issue_reporter") + " " + b64(getPrefix(TAG_Semangit + TAG_Userprefix) + curLine[2]) + ";");
                writer.newLine();
                if(!curLine[3].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_issue_assignee") + " " + b64(getPrefix(TAG_Semangit + TAG_Userprefix) + curLine[3]) + ";");
                    writer.newLine();
                }
                if(!curLine[5].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_issue_pull_request") + " " + b64(getPrefix(TAG_Semangit + TAG_Pullrequestprefix) + curLine[5]) + ";");
                    writer.newLine();
                }
                writer.write(getPrefix(TAG_Semangit + "github_issue_created_at") + " \"" + curLine[6] + "\".");
                writer.newLine();
                curLine = nextLine;

            }
            //Handle last line
            String issueURL = b64(getPrefix(TAG_Semangit + TAG_Issueprefix) + curLine[7]);
            writer.write( issueURL + " a " + getPrefix(TAG_Semangit + "github_issue") + ";");
            writer.newLine();
            writer.write(getPrefix(TAG_Semangit + "github_issue_project") + " " + b64(getPrefix(TAG_Semangit + TAG_Repoprefix) + curLine[1]) + ";");
            writer.newLine();
            writer.write(getPrefix(TAG_Semangit + "github_issue_reporter") + " " + b64(getPrefix(TAG_Semangit + TAG_Userprefix) + curLine[2]) + ";");
            writer.newLine();
            if(!curLine[3].equals("N"))
            {
                writer.write(getPrefix(TAG_Semangit + "github_issue_assignee") + " " + b64(getPrefix(TAG_Semangit + TAG_Userprefix) + curLine[3]) + ";");
                writer.newLine();
            }
            if(!curLine[5].equals("N"))
            {
                writer.write(getPrefix(TAG_Semangit + "github_issue_pull_request") + " " + b64(getPrefix(TAG_Semangit + TAG_Pullrequestprefix) + curLine[5]) + ";");
                writer.newLine();
            }
            writer.write(getPrefix(TAG_Semangit + "github_issue_created_at") + " \"" + curLine[6] + "\".");
            writer.newLine();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);

        }
    }

    private static void parseOrganizationMembers(String path)
    {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "organization_members.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/organization_members.ttl"), 32768);
            String[] nextLine;
            
            while((nextLine = reader.readNext())!= null) {

                if(nextLine.length != 3) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }

                writer.write("[ a " + getPrefix(TAG_Semangit + "github_organization_join_event") + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_organization_joined_at") + " \"" + nextLine[2] + "\" ] " + getPrefix(TAG_Semangit + "github_organization_joined_by") + " " + getPrefix(TAG_Semangit  + TAG_Userprefix) + nextLine[1] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_organization_is_joined") + " " + b64(getPrefix(TAG_Semangit  + TAG_Userprefix) + nextLine[0]) + ".");
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



    private static void parseProjectCommits(String path)
    {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "project_commits.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/project_commits.ttl"), 32768);
            String[] nextLine;
            
            String[] curLine = reader.readNext();
            boolean abbreviated = false;
            while((nextLine = reader.readNext())!= null) {


                if(nextLine.length != 2) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }

                if(abbreviated) //abbreviated in previous step. Only need to print object now
                {
                    writer.write(b64(getPrefix(TAG_Semangit + TAG_Repoprefix) + curLine[1])); //one commit for multiple repositories (branching / merging)
                }
                else //no abbreviation occurred. Full subject predicate object triple printed
                {
                    writer.write(b64(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[0]) + " " + getPrefix(TAG_Semangit + "repository_has_commit") + " " + b64(getPrefix(TAG_Semangit + TAG_Repoprefix) + curLine[1]));
                }

                abbreviated = (curLine[0].equals(nextLine[0]));
                curLine = nextLine;
                if(abbreviated)
                {
                    writer.write(",");
                    //no new line to save space
                }
                else {
                    writer.write(".");
                    writer.newLine();
                }
            }



            //handle last line
            if(abbreviated) //abbreviated in previous step. Only need to print object now
            {
                writer.write(b64(getPrefix(TAG_Semangit + TAG_Repoprefix) + curLine[1]) + "."); //one commit for multiple repositories (branching / merging)
            }
            else //no abbreviation occurred. Full subject predicate object triple printed
            {
                writer.write(b64(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[0]) + " " + getPrefix(TAG_Semangit + "repository_has_commit") + " " + b64(getPrefix(TAG_Semangit + TAG_Repoprefix) + curLine[1]) + ".");
            }

            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }




    private static void parseProjectMembers(String path)
    {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "project_members.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/project_members.ttl"), 32768);
            String[] nextLine;
            
            while((nextLine = reader.readNext())!= null) {

                if(nextLine.length != 4) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }

                writer.write("[ a " + getPrefix(TAG_Semangit + "github_project_join_event") + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_project_join_event_created_at") + " \"" + nextLine[2] + "\" ] ");
                writer.write(getPrefix(TAG_Semangit + "github_project_joining_user") + " " + b64(getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[1]) + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_project_joined") + " " + b64(getPrefix(TAG_Semangit + TAG_Repoprefix) + nextLine[0]) + ".");
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




    private static void parseProjects(String path)
    {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "projects.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/projects.ttl"), 32768);
            String[] nextLine;
            
            while((nextLine = reader.readNext())!= null) {


                if(nextLine.length != 11) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }

                for (int i = 0; i < nextLine.length; i++) {
                    nextLine[i] = groovy.json.StringEscapeUtils.escapeJava(nextLine[i]);
                }
                writer.write(b64(getPrefix(TAG_Semangit + TAG_Repoprefix) + nextLine[0]) + " a " + getPrefix(TAG_Semangit + "github_project") + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "repository_url") + " \"" + nextLine[1] + "\";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_has_owner") + " " + b64(getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[2]) + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_project_name") + " \"" + nextLine[3] + "\";");
                writer.newLine();
                if(!nextLine[4].equals("")) {

                    writer.write(getPrefix(TAG_Semangit + "github_project_description") + " \"" + nextLine[4] + "\";");
                    writer.newLine();
                }
                if(!nextLine[5].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "repository_language") + " \"" + nextLine[5] + "\";");
                    writer.newLine();
                }
                if(!nextLine[7].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_forked_from") + " " + b64(getPrefix(TAG_Semangit + TAG_Repoprefix) + nextLine[7]) + ";");
                    writer.newLine();
                }
                if(nextLine[8].equals("1"))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_project_deleted") + " true;");
                    writer.newLine();
                }
                else
                {
                    writer.write(getPrefix(TAG_Semangit + "github_project_deleted") + " false;");
                    writer.newLine();
                }
                //Not taking "last update" into account, as we can easily compute that on a graph database
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


    private static void parsePullRequestCommits(String path)
    {
        try
        {
            CSVReader reader = new CSVReader(new FileReader(path + "pull_request_commits.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/pull_request_commits.ttl"), 32768);
            String[] nextLine;
            
            String[] curLine = reader.readNext();
            boolean abbreviated = false;
            while ((nextLine = reader.readNext()) != null) {

                if(nextLine.length != 2) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }

                if(abbreviated)
                {
                    writer.write(b64(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[1]));
                }
                else
                {
                    writer.write(b64(getPrefix(TAG_Semangit + TAG_Pullrequestprefix) + curLine[0]) + " " + getPrefix(TAG_Semangit + "pull_request_has_commit") + " " + b64(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[1]));
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
                writer.write(b64(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[1]) + ".");
            }
            else
            {
                writer.write(b64(getPrefix(TAG_Semangit + TAG_Pullrequestprefix) + curLine[0]) + " " + getPrefix(TAG_Semangit + "pull_request_has_commit") + " " + b64(getPrefix(TAG_Semangit + TAG_Commitprefix) + curLine[1]) + ".");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    private static void parsePullRequestHistory(String path) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "pull_request_history.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/pull_request_history.ttl"), 32768);
            String[] nextLine;
            
            while ((nextLine = reader.readNext()) != null) {
                if(nextLine.length != 5) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }
                writer.write("[ a " + getPrefix(TAG_Semangit + "github_pull_request_action") + ";");
                writer.newLine();
                //id, PR id, created at, action, actor
                writer.write(getPrefix(TAG_Semangit + "github_pull_request_action_created_at") + " \"" + nextLine[2] + "\";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_pull_request_action_id") + " " + nextLine[0] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_pull_request_action_type") + " \"" + nextLine[3] + "\" ] ");
                if(!nextLine[4].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_pull_request_actor") + " " + b64(getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[4]) + ";");
                    writer.newLine();
                }
                if(!nextLine[1].equals("N")) {
                    writer.write(getPrefix(TAG_Semangit + "github_pull_request_action_pull_request") + " " + b64(getPrefix(TAG_Semangit + TAG_Pullrequestprefix) + nextLine[1]) + ".");
                    writer.newLine();
                }
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }




    private static void parsePullRequests(String path) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "pull_requests.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/pull_requests.ttl"), 32768);
            String[] nextLine;
            
            while ((nextLine = reader.readNext()) != null) {

                if(nextLine.length != 7) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }
                writer.write(b64(getPrefix(TAG_Semangit + TAG_Pullrequestprefix) + nextLine[0]) + " a " + getPrefix(TAG_Semangit + "github_pull_request") + ";");
                writer.newLine();
                if(!nextLine[2].equals("N")) {
                    writer.write(getPrefix(TAG_Semangit + "pull_request_base_project") + " " + b64(getPrefix(TAG_Semangit + TAG_Repoprefix) + nextLine[2]) + ";");
                    writer.newLine();
                }
                if(!nextLine[1].equals("N")) {
                    writer.write(getPrefix(TAG_Semangit + "pull_request_head_project") + " " + b64(getPrefix(TAG_Semangit + TAG_Repoprefix) + nextLine[1]) + ";");
                    writer.newLine();
                }
                if(!nextLine[4].equals("N")) {
                    writer.write(getPrefix(TAG_Semangit + "pull_request_base_commit") + " " + b64(getPrefix(TAG_Semangit + TAG_Commitprefix) + nextLine[4]) + ";");
                    writer.newLine();
                }
                if(!nextLine[3].equals("N")) {
                    writer.write(getPrefix(TAG_Semangit + "pull_request_head_commit") + " " + b64(getPrefix(TAG_Semangit + TAG_Commitprefix) + nextLine[3]) + ";");
                    writer.newLine();
                }
                if(!nextLine[5].equals("N")) {
                    writer.write(getPrefix(TAG_Semangit + "github_pull_request_id") + " " + nextLine[5] + ";");
                    writer.newLine();
                }
                writer.write(getPrefix(TAG_Semangit + "github_pull_request_intra_branch") + " ");
                if(nextLine[6].equals("0"))
                {
                    writer.write("false");
                }
                else{
                    writer.write("true");
                }
                writer.write(".");
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void parseRepoLabels(String path)
    {
        try
        {
            CSVReader reader = new CSVReader(new FileReader(path + "repo_labels.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/repo_labels.ttl"), 32768);
            String[] nextLine;
            
            while((nextLine = reader.readNext())!= null)
            {
                if(nextLine.length != 3) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }
                for (int i = 0; i < nextLine.length; i++) {
                    nextLine[i] = groovy.json.StringEscapeUtils.escapeJava(nextLine[i]);
                }
                writer.write(b64(getPrefix(TAG_Semangit + TAG_Repolabelprefix) + nextLine[0]) + " a " + getPrefix(TAG_Semangit + "github_repo_label") + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_repo_label_project") + " " + b64(getPrefix(TAG_Semangit + TAG_Repoprefix) + nextLine[1]) + ";");
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


    private static void parseRepoMilestones(String path)
    {
        try
        {
            CSVReader reader = new CSVReader(new FileReader(path + "repo_milestones.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/repo_milestones.ttl"), 32768);
            String[] nextLine;
            
            while((nextLine = reader.readNext())!= null)
            {
                for (int i = 0; i < nextLine.length; i++) {
                    nextLine[i] = groovy.json.StringEscapeUtils.escapeJava(nextLine[i]);
                }
                //TODO! repo_milestones is empty in all dumps?! Cannot convert without data...
            }
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }




    private static void parseUsers(String path)
    {
        try
        {
            CSVReader reader = new CSVReader(new FileReader(path + "users.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/users.ttl"), 32768);
            String[] nextLine;
            
            while((nextLine = reader.readNext())!= null)
            {
                //13 lines now! The parser won't work on heavily outdated dumps, as the structure was modified at some point!!!
                if(nextLine.length != 13) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }
                for (int i = 0; i < nextLine.length; i++) {
                    nextLine[i] = groovy.json.StringEscapeUtils.escapeJava(nextLine[i]);
                }

                //System.out.println(b64(getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[0]));
                String userURI = b64(getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[0]);
                writer.write(userURI + " a " + getPrefix(TAG_Semangit + "github_user") + ";");
                writer.newLine();
                if(!nextLine[1].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_login") + " \"" + nextLine[1] + "\";");
                    writer.newLine();
                }
                /*if(!nextLine[2].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_name") + " \"" + nextLine[2] + "\";");
                    writer.newLine();
                }*/
                if(!nextLine[2].equals("N"))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_company") + " \"" + nextLine[2] + "\";");
                    writer.newLine();
                }
                if(!nextLine[3].equals("N"))
                writer.write(getPrefix(TAG_Semangit + "github_user_created_at") + " \"" + nextLine[3] + "\";");
                writer.newLine();

                writer.write(getPrefix(TAG_Semangit + "github_user_is_org") + " ");
                if(nextLine[4].equals("USR"))
                {
                    writer.write("false;");
                    writer.newLine();
                }
                else
                {
                    writer.write("true;");
                    writer.newLine();
                }

                writer.write(getPrefix(TAG_Semangit + "github_user_deleted") + " ");
                if(nextLine[5].equals("0"))
                {
                    writer.write("false;");
                    writer.newLine();
                }
                else
                {
                    writer.write("true;");
                    writer.newLine();
                }
                writer.write(getPrefix(TAG_Semangit + "github_user_fake") + " ");
                if(nextLine[6].equals("0"))
                {
                    writer.write("false.");
                    writer.newLine();
                }
                else
                {
                    writer.write("true.");
                    writer.newLine();
                }

                if(!nextLine[7].equals("N") && !nextLine[7].equals(""))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_user_lng") + " \"" + nextLine[7] + "\";");
                    writer.newLine();
                }
                if(!nextLine[8].equals("N") && !nextLine[8].equals(""))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_user_lat") + " \"" + nextLine[8] + "\";");
                    writer.newLine();
                }

                if(!nextLine[9].equals("N") && !nextLine[9].equals(""))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_user_country_code") + " \"" + nextLine[9] + "\";");
                    writer.newLine();
                }

                if(!nextLine[10].equals("N") && !nextLine[10].equals(""))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_user_state") + " \"" + nextLine[10] + "\";");
                    writer.newLine();
                }

                if(!nextLine[11].equals("N") && !nextLine[11].equals(""))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_user_city") + " \"" + nextLine[11] + "\";");
                    writer.newLine();
                }

                if(!nextLine[12].equals("N") && !nextLine[12].equals(""))
                {
                    writer.write(getPrefix(TAG_Semangit + "github_user_location") + " \"" + nextLine[12] + "\";");
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

    private static void parseWatchers(String path)
    {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "watchers.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/watchers.ttl"), 32768);
            String[] nextLine;
            
            while((nextLine = reader.readNext())!= null) {
                if(nextLine.length != 3) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }
                writer.write("[ a " + getPrefix(TAG_Semangit + "github_follow_event") + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_following_since") + " \"" + nextLine[2] + "\";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_user_or_project") + " true ] " + getPrefix(TAG_Semangit + "github_follower") + " " + b64(getPrefix(TAG_Semangit  + TAG_Userprefix) + nextLine[1]) + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_follows") + " " + b64(getPrefix(TAG_Semangit  + TAG_Repoprefix) + nextLine[0]) + ".");
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




    //project_languages

    private static void parseProjectLanguages(String path)
    {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "project_languages.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/project_languages.ttl"), 32768);
            String[] nextLine;
            Map<String, Integer> languages = new HashMap<>();
            int langCtr = 0;
            int currentLang;
            while((nextLine = reader.readNext())!= null) {
                if(nextLine.length != 4) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }
                if(languages.containsKey(nextLine[1])) {
                    currentLang = languages.get(nextLine[1]);
                }
                else
                {
                    languages.put(nextLine[1], langCtr++);
                    writer.write(b64(getPrefix(TAG_Semangit + TAG_Langprefix) + langCtr) + " a " + getPrefix(TAG_Semangit + "programming_language") + ";");
                    writer.newLine();
                    writer.write(getPrefix(TAG_Semangit + "programming_language_name") + " \"" + nextLine[1] + "\".");
                    writer.newLine();
                    currentLang = langCtr;
                }
                writer.write("[ a " + getPrefix(TAG_Semangit + "github_project_language") + ";");
                writer.newLine();
                //bytes, timestamp, then close brackets and do remaining two links
                writer.write(getPrefix(TAG_Semangit + "github_project_language_bytes") + " " + nextLine[2] + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_project_language_timestamp") + " \"" + nextLine[3] + "\"] " + getPrefix(TAG_Semangit + "github_project_language_repo") + " " + b64(getPrefix(TAG_Semangit + TAG_Repoprefix) + nextLine[0]) + ";");
                writer.newLine();
                writer.write(getPrefix(TAG_Semangit + "github_project_language_is") + " " + b64(getPrefix(TAG_Semangit + TAG_Langprefix) + currentLang));
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


    //parseProjectTopics added as .csv file, but undocumented! Skipping this tiny file




    /**
     * Comment section. Below are all functions related to comments.
     * commit_comments
     * issue_comments
     * pull_request_comments
     */

    private static void parseCommitComments(String path) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "commit_comments.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/commit_comments.ttl"), 32768);
            String[] nextLine;
            String rightOfComma;
            while ((nextLine = reader.readNext()) != null) {

                if(nextLine.length != 8) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }
                try //check if all numeric fields are indeed numeric. If not, it's a broken line inside the csv file
                {
                    rightOfComma = nextLine[0].substring(nextLine[0].lastIndexOf(":") + 1);
                    Integer.parseInt(rightOfComma);
                    rightOfComma = nextLine[1].substring(nextLine[1].lastIndexOf(":") + 1);
                    Integer.parseInt(rightOfComma);
                    rightOfComma = nextLine[2].substring(nextLine[2].lastIndexOf(":") + 1);
                    Integer.parseInt(rightOfComma);
                    if (!nextLine[4].equals("N") && !nextLine[4].equals("")) {
                        rightOfComma = nextLine[4].substring(nextLine[4].lastIndexOf(":") + 1);
                        Integer.parseInt(rightOfComma);
                    }
                    if (!nextLine[5].equals("N") && !nextLine[5].equals("")) {
                        rightOfComma = nextLine[5].substring(nextLine[5].lastIndexOf(":") + 1);
                        Integer.parseInt(rightOfComma);
                    }
                    if (!nextLine[6].equals("N") && !nextLine[6].equals("")) {
                        rightOfComma = nextLine[6].substring(nextLine[6].lastIndexOf(":") + 1);
                        Integer.parseInt(rightOfComma);
                    }
                }
                catch (NumberFormatException e) //broken csv file
                {
                    continue;
                }


                for (int i = 0; i < nextLine.length; i++) {
                    nextLine[i] = groovy.json.StringEscapeUtils.escapeJava(nextLine[i]);
                }
                writer.write(b64(getPrefix(TAG_Semangit + TAG_Commentprefix + "commit_") + nextLine[0]) + " a " + getPrefix(TAG_Semangit + "comment") + ";");
                writer.newLine();
                if(!nextLine[1].equals("N") && !nextLine[1].equals("")){
                    writer.write(getPrefix(TAG_Semangit + "comment_for") + " " + b64(getPrefix(TAG_Semangit + TAG_Commitprefix) + nextLine[1]) + ";"); //comment for a commit
                    writer.newLine();
                }
                if(!nextLine[2].equals("N") && !nextLine[2].equals("")) {
                    writer.write(getPrefix(TAG_Semangit + "comment_author") + " " + b64(getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[2]) + ";");
                    writer.newLine();
                }
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


    private static void parseIssueComments(String path) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "issue_comments.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/issue_comments.ttl"), 32768);
            String[] nextLine;
            
            while ((nextLine = reader.readNext()) != null) {
                /*for (int i = 0; i < nextLine.length; i++) {
                    nextLine[i] = groovy.json.StringEscapeUtils.escapeJava(nextLine[i]);
                }*/
                if(nextLine.length != 4) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }

                //TODO: Let's verify the integrity of the RDF output of this
                writer.write("[" + getPrefix(TAG_Semangit + "comment_for") + " " + b64(getPrefix(TAG_Semangit + TAG_Issueprefix) + nextLine[0]) + ";"); //comment for an issue
                writer.newLine();
                
                    writer.write(getPrefix(TAG_Semangit + "comment_created_at") + " \"" + nextLine[3] + "\";");
                    writer.newLine();
                
                if(!nextLine[1].equals("") && !nextLine[1].equals("N")) {
                    writer.write(getPrefix(TAG_Semangit + "comment_author") + " " + b64(getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[1]) + "] a " + getPrefix(TAG_Semangit + "comment") + ".");
                    writer.newLine();
                }
                else
                {
                    System.out.println("Warning! Invalid user found in parseIssueComments. Using MAX_INT as userID.");
                    writer.write(getPrefix(TAG_Semangit + "comment_author") + " " + b64(getPrefix(TAG_Semangit + TAG_Userprefix) + Integer.MAX_VALUE + "] a " + getPrefix(TAG_Semangit + "comment") + "."));//comment for [0]
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



    private static void parsePullRequestComments(String path) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path + "pull_request_comments.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/pull_request_comments.ttl"), 32768);
            String[] nextLine;
            String rightOfComma;
            while ((nextLine = reader.readNext()) != null) {

                if(nextLine.length != 7) //skip corrupted lines to prevent broken rdf
                {
                    continue;
                }
                try //check if all numeric fields are indeed numeric. If not, it's a broken line inside the csv file
                {
                    rightOfComma = nextLine[0].substring(nextLine[0].lastIndexOf(":") + 1);
                    Integer.parseInt(rightOfComma);
                    rightOfComma = nextLine[1].substring(nextLine[1].lastIndexOf(":") + 1);
                    Integer.parseInt(rightOfComma);
                    rightOfComma = nextLine[2].substring(nextLine[2].lastIndexOf(":") + 1);
                    Integer.parseInt(rightOfComma);
                    rightOfComma = nextLine[3].substring(nextLine[3].lastIndexOf(":") + 1);
                    Integer.parseInt(rightOfComma);
                    rightOfComma = nextLine[5].substring(nextLine[5].lastIndexOf(":") + 1);
                    Integer.parseInt(rightOfComma);
                }
                catch (NumberFormatException e) //broken csv file
                {
                    continue;
                }
                
                for (int i = 0; i < nextLine.length; i++) {
                    nextLine[i] = groovy.json.StringEscapeUtils.escapeJava(nextLine[i]);
                }
                //TODO: Let's verify the integrity of the RDF output of this
                if(!nextLine[0].equals("")) {
                    writer.write("[" + getPrefix(TAG_Semangit + "comment_for") + " " + b64(getPrefix(TAG_Semangit + TAG_Pullrequestprefix) + nextLine[0])); //comment for a pull request                     
                    writer.write(",");
                    writer.newLine();
                }
                else
                {
                    writer.write("[" + getPrefix(TAG_Semangit + "comment_for") + " ");
                }
                if(!nextLine[5].equals("") && !nextLine[5].equals("N")) {
                    writer.write(b64(getPrefix(TAG_Semangit + TAG_Commitprefix) + nextLine[5]) + ";");
                    writer.newLine();
                }
                if(!nextLine[6].equals("") && !nextLine[6].equals("N")) {
                    writer.write(getPrefix(TAG_Semangit + "comment_created_at") + " \"" + nextLine[6] + "\";");
                    writer.newLine();
                }
                if(!nextLine[3].equals("") && !nextLine[3].equals("N")) {
                    writer.write(getPrefix(TAG_Semangit + "comment_pos") + " " + nextLine[3] + ";");
                    writer.newLine();
                }
                if(!nextLine[4].equals("") && !nextLine[4].equals("N")) {

                    writer.write(getPrefix(TAG_Semangit + "comment_body") + " \"" + nextLine[4] + "\";");
                    writer.newLine();
                }
                if(!nextLine[1].equals("") && !nextLine[1].equals("N")) {
                    writer.write(getPrefix(TAG_Semangit + "comment_author") + " " + b64(getPrefix(TAG_Semangit + TAG_Userprefix) + nextLine[1]) + "] a " + getPrefix(TAG_Semangit + "comment") + ".");
                    writer.newLine();
                }
                else
                {
                    System.out.println("Warning! Invalid user found in parsePullRequestComments. Using MAX_INT as userID.");
                    writer.write(getPrefix(TAG_Semangit + "comment_author") + " " + b64(getPrefix(TAG_Semangit + TAG_Userprefix) + Integer.MAX_VALUE + "] a " + getPrefix(TAG_Semangit + "comment") + "."));//comment for [0]
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
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outPath), 32768);
                final Set<Map.Entry<String, String>> entries = prefixTable.entrySet();
                writer.write("@prefix semangit: <http://semangit.de/ontology/>.");
                for(Map.Entry<String, String> entry : entries)
                {
                    writer.write("@prefix " + entry.getValue() + ": <http://semangit.de/ontology/" + entry.getKey() + "#>.");
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

    private MainClass(String workOnFile, String path)
    {
        this.workOnFile = workOnFile;
        this.path = path;
    }

    public void run()
    {
        if(this.workOnFile == null)
        {
            throw(new RuntimeException("You need to define a file to work on!"));
        }
        switch ( workOnFile )
        {
            case "commit_comments": parseCommitComments(this.path);break;
            case "commit_parents": parseCommitParents(this.path);break;
            case "commits": parseCommits(this.path);break;
            case "followers": parseFollowers(this.path);break;
            case "issue_comments": parseIssueComments(this.path);break;
            case "issue_events": parseIssueEvents(this.path);break;
            case "issue_labels": parseIssueLabels(this.path);break;
            case "issues": parseIssues(this.path);break;
            case "organization_members": parseOrganizationMembers(this.path);break;
            case "project_commits": parseProjectCommits(this.path);break;
            case "project_languages": parseProjectLanguages(this.path);break;
            case "project_members": parseProjectMembers(this.path);break;
            case "projects": parseProjects(this.path);break;
            case "pull_request_comments": parsePullRequestComments(this.path);break;
            case "pull_request_commits": parsePullRequestCommits(this.path);break;
            case "pull_request_history": parsePullRequestHistory(this.path);break;
            case "pull_requests": parsePullRequests(this.path);break;
            case "users": parseUsers(this.path);break;
            case "repo_labels": parseRepoLabels(this.path);break;
            case "repo_milestones":parseRepoMilestones(this.path);break;
            case "watchers":parseWatchers(this.path);break;
            default: throw new RuntimeException("Unknown file name specified! Which file to parse?!");
        }
        //System.out.println("Finished working on " + this.workOnFile);
    }

    public static void main(String[] args)
    {
        if(args.length > 1)
        {
            //mode, prefixing
            for(String s: args)
            {
                if(s.equals("-noprefix"))
                {
                    System.out.println("Prefixing disabled! Large output expected...");
                    prefixing = false;
                }
                if(s.contains("-base="))
                {
                    String rightOfEql = s.substring(s.lastIndexOf("=") + 1);
                    int in = Integer.parseInt(rightOfEql);
                    if(in == 64)
                    {
                        System.out.println("Using Base64URL representation for integers.");
                        mode = 0;
                    }
                    else if(in == 32)
                    {
                        System.out.println("Using Base32 representation for integers.");
                        mode = 1;
                    }
                    else if(in == 16)
                    {
                        System.out.println("Using Base16 representation for integers.");
                        mode = 2;
                    }
                    else if(in == 10)
                    {
                        System.out.println("Using Base10 representation for integers.");
                        mode = 3;
                    }
                    else
                    {
                        System.out.println("Unknown base passed as argument. Using Base64URL representation for integers.");
                        mode = 0;
                    }
                }
            }
            if(!prefixing && (mode != 3))
            {
                System.out.println("No Prefixing option is only available for base10. Setting base to 10.");
                mode = 3;
            }
        }
        try {
            File index = new File(args[0] + "rdf");
            //will take care of the deletion via bash
            /*if (index.exists()) {
                System.out.println("rdf/ already exists. Deleting!");
                String[] entries = index.list();
                if (entries != null) {
                    for (String s : entries) {
                        File currentFile = new File(index.getPath(), s);
                        if (!currentFile.delete()) {
                            System.out.println("Failed to delete existing file: " + index.getPath() + s);
                            System.exit(1);
                        }
                    }
                }
                if (!index.delete()) {
                    System.out.println("Unable to delete rdf/ directory after deleting all entries.");
                    System.exit(1);
                }
            }*/
            if (!index.exists() && !index.mkdirs()) {
                System.out.println("Unable to create " + args[0] + "rdf/ directory. Exiting.");
                System.exit(1);
            }

            initPrefixTable();

            ArrayList<Thread> processes = new ArrayList<>();
            processes.add(new Thread(new MainClass("project_commits", args[0])));
            processes.add(new Thread(new MainClass("commit_comments", args[0])));
            processes.add(new Thread(new MainClass("commit_parents", args[0])));
            processes.add(new Thread(new MainClass("commits", args[0])));
            processes.add(new Thread(new MainClass("followers", args[0])));
            processes.add(new Thread(new MainClass("issue_comments", args[0])));
            processes.add(new Thread(new MainClass("issue_events", args[0])));
            processes.add(new Thread(new MainClass("issue_labels", args[0])));
            processes.add(new Thread(new MainClass("issues", args[0])));
            processes.add(new Thread(new MainClass("organization_members", args[0])));
            processes.add(new Thread(new MainClass("project_members", args[0])));
            processes.add(new Thread(new MainClass("project_languages", args[0])));
            processes.add(new Thread(new MainClass("projects", args[0])));
            processes.add(new Thread(new MainClass("pull_request_comments", args[0])));
            processes.add(new Thread(new MainClass("pull_request_commits", args[0])));
            processes.add(new Thread(new MainClass("pull_request_history", args[0])));
            processes.add(new Thread(new MainClass("pull_requests", args[0])));
            processes.add(new Thread(new MainClass("users", args[0])));
            processes.add(new Thread(new MainClass("repo_labels", args[0])));
            processes.add(new Thread(new MainClass("repo_milestones", args[0])));
            processes.add(new Thread(new MainClass("watchers", args[0])));
            for(Thread t : processes)
            {
                try {
                    t.start();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    errorCtr++;
                }
            }
            for (Thread t : processes)
            {
                try{
                    t.join();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    errorCtr++;
                }
            }

            String correctPath = args[0].concat("rdf/");
            appendFileToOutput(correctPath, "project_commits.ttl");
            appendFileToOutput(correctPath, "commit_comments.ttl");
            appendFileToOutput(correctPath, "commits.ttl");
            appendFileToOutput(correctPath, "commit_parents.ttl");
            appendFileToOutput(correctPath, "issue_comments.ttl");
            appendFileToOutput(correctPath, "pull_request_comments.ttl");
            appendFileToOutput(correctPath, "issue_events.ttl");
            appendFileToOutput(correctPath, "issues.ttl");
            appendFileToOutput(correctPath, "project_members.ttl");
            appendFileToOutput(correctPath, "project_languages.ttl");
            appendFileToOutput(correctPath, "projects.ttl");
            appendFileToOutput(correctPath, "pull_request_history.ttl");
            appendFileToOutput(correctPath, "pull_request_commits.ttl");
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
        if(errorCtr != 0)
        {
            System.out.println("A total of " + errorCtr + " errors occurred.");
            System.exit(2);
        }
        System.exit(0);

    }
}
