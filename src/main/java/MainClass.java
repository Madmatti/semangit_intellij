import com.opencsv.CSVReader;

import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.header.Header;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.apache.commons.lang3.StringEscapeUtils;


import java.io.*;

public class MainClass {

    private static final String TAG_Semangit = "semangit:";
    private static final String TAG_Userprefix = "ghuser_";
    public static void parseUsers(String path)
    {
        try
        {
            CSVReader reader = new CSVReader(new FileReader(path + "users.csv"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "rdf/users.ttl"), 32768);
            String[] nextLine;
            writer.write("@prefix semangit: <http://www.dennis_stinkt_krass.pizza#> .");
            writer.newLine();
            writer.newLine();
            String quot = (Character.toString('"'));
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

    public static void rdf2hdtUsers(String path)
    {
        String baseURI = "http://example.com/mydataset";
        String rdfInput = path.concat("rdf/users.ttl");
        String inputType = "turtle";

        String hdtOutput = path.concat("hdt/users.hdt");
        try
        {
            HDT hdt = HDTManager.generateHDT(rdfInput, baseURI, RDFNotation.parse(inputType), new HDTSpecification(), null);
            hdt.saveToHDT(hdtOutput, null);
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
        parseUsers(args[0]);

        rdf2hdtUsers(args[0]);


        System.exit(0);
    }
}
