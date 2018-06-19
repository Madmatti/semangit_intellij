import com.opencsv.CSVReader;

import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.hdt.*;
import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.apache.commons.lang3.StringEscapeUtils;
import org.rdfhdt.hdt.rdf.RDFParserCallback;
import org.rdfhdt.hdt.rdf.RDFParserFactory;
import org.rdfhdt.hdt.tools.RDF2HDT;


import java.io.*;

public class MainClass {

    private static final String PREFIX_Semangit = "<http://www.sg.com/ont/>";
    private static final String TAG_Semangit = "semangit:";
    private static final String TAG_Userprefix = "ghuser_";
    public static void parseUsers(String path, boolean includePrefix)
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


    public static void parseOrganizationMembers(String path, boolean includePrefix)
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


    public static void parseFollowers(String path, boolean includePrefix)
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


    public static void rdf2hdt(String path, String table)
    {
        String baseURI = "http://example.com/mydataset";
        String rdfInput = path.concat("rdf/" + table + ".ttl");
        String inputType = "turtle";

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
    }

    public static void appendFileToOutput(String directory, String fileName)
    {

        String outPath = directory.concat("combined.ttl");
        try(BufferedReader br = new BufferedReader(new FileReader(directory.concat(fileName)))) {
            Writer output = new BufferedWriter(new FileWriter(outPath, true));
            for(String line; (line = br.readLine()) != null; ) {
                output.append(line);
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

    public static void main(String[] args)
    {
        new File(args[0] + "rdf").mkdirs();
        new File(args[0] + "hdt").mkdirs();

        parseOrganizationMembers(args[0], true);
        //rdf2hdt(args[0], "organization_members");
        System.out.println("Organizations parsed.");

        parseFollowers(args[0], false);
        System.out.println("Followers parsed.");

        parseUsers(args[0], false);
        System.out.println("Users parsed.");

        try {
            String correctPath = args[0].concat("rdf/");
            System.out.println("Appending Org Members");
            appendFileToOutput(correctPath, "organization_members.ttl");

            System.out.println("Appending followers");
            appendFileToOutput(correctPath, "followers.ttl");

            System.out.println("Appending users");
            appendFileToOutput(correctPath, "users.ttl");

            /*
            System.out.println("Reading organization Members into memory");
            String file1str = org.apache.commons.io.FileUtils.readFileToString(file1);
            //String file2str = org.apache.commons.io.FileUtils.readFileToString(file2);
            file1 = null;
            System.out.println("Done! Writing to file...");
            org.apache.commons.io.FileUtils.write(file4, file1str);
            file1str = "";

            System.out.println("Reading followers into memory");
            String file2str = org.apache.commons.io.FileUtils.readFileToString(file2);
            file2 = null;
            System.out.println("Done! Writing to file...");
            org.apache.commons.io.FileUtils.write(file4, file2str);
            file2str = "";

            System.out.println("Reading users into memory");
            String file3str = org.apache.commons.io.FileUtils.readFileToString(file3);
            file3 = null;
            System.out.println("Done! Writing to file...");
            org.apache.commons.io.FileUtils.write(file4, file3str);
            file3str = "";*/

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        //rdf2hdt(args[0], "users");


        /*String thePath = args[0].concat("hdt/");
        try {
            HDT hdt = HDTManager.loadHDT(thePath.concat("organization_members.hdt"));
            HDTManager.mapHDT(thePath.concat("users.hdt"));
            RDF2HDT test = new RDF2HDT();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }*/

        System.exit(0);
    }
}
