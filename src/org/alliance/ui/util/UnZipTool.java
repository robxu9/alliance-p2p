package org.alliance.ui.util;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class UnZipTool {
   public static final void writeFile(InputStream in, OutputStream out)
   throws IOException {
byte[] buffer = new byte[1024];
int len;

while ((len = in.read(buffer)) >= 0)
   out.write(buffer, 0, len);

in.close();
out.close();
}

public static void unZip(String zipFileName,
   String directoryToExtractTo) {
Enumeration entriesEnum;
ZipFile zipFile;
File newDir;
directoryToExtractTo += "updates/";
try {
   zipFile = new ZipFile(zipFileName);
   entriesEnum = zipFile.entries();

   File directory= new File(directoryToExtractTo);

   /**
    * Check if the directory to extract to exists
    */
   if(!directory.exists())
   {
       /**
        * If not, create a new one.
        */
       new File(directoryToExtractTo).mkdir();
       System.err.println("...Directory Created -"+directoryToExtractTo);
   }
   while (entriesEnum.hasMoreElements()) {
       try {
           ZipEntry entry = (ZipEntry) entriesEnum.nextElement();
           
           if (entry.isDirectory()) {
              // @ToDo Does nothing at the moment.
           } 
           else if(!entry.getName().contains("svn")) {

               System.out.println("Extracting file: "
                       + entry.getName());
               /**
                * The following logic will just extract the file name
                * and discard the directory
                */
               int index = 0;
               String name = entry.getName();
               index = entry.getName().lastIndexOf("/");
               if (index > 0 && index != name.length())
                   name = entry.getName().substring(index + 1);

        

               writeFile(zipFile.getInputStream(entry),
                       new BufferedOutputStream(new FileOutputStream(
                               directoryToExtractTo + name)));
           }
           else{
        	   System.err.println("----Intentionally did not Extract: " + entry.getName());
           }
       } 
       catch (Exception e) {
           e.printStackTrace();
       }
       //Delete Zip
       boolean zipFileDelete = new File(zipFileName).delete();
       if(zipFileDelete)
       System.out.println("********DELETED ZIP FILE*******");
   }

   zipFile.close();
} catch (IOException ioe) {
   System.err.println("Some Exception Occurred:");
   ioe.printStackTrace();
   return;
}
}
}