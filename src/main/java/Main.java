import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//-Xmx4096m run using this vm option.

public  class Main {
    public static void main(String[] args) throws Exception{
            String usage = "java "
                    + " [-index INDEX_PATH] [-docs DOCS_PATH] [-queries file] [-numdocs hitsPerPage]\n\n"
                    + "This indexes the documents in DOCS_PATH, creating a Lucene index"
                    + "in INDEX_PATH that can be searched with SearchFiles";
            String index = "index";
            String docsPath = null;
            String queries = null;
            int numdocs = 10;

            for (int i = 0; i < args.length; i++) {
                if ("-index".equals(args[i])) {
                    index = args[i + 1];
                    i++;
                } else if ("-docs".equals(args[i])) {
                    docsPath = args[i + 1];
                    i++;
                }
                else if ("-queries".equals(args[i])) {
                    queries = args[i + 1];
                    i++;
                } else if ("-numdocs".equals(args[i])) {
                    numdocs = Integer.parseInt(args[i + 1]);
                    if (numdocs <= 0) {
                        System.err.println("There must be at least 1 hit per page.");
                        System.exit(1);
                    }
                    i++;
                }
            }
            
            if (docsPath == null || queries == null) {
                System.err.println("Usage: " + usage);
                System.exit(1);
            }

            final Path docDir = Paths.get(docsPath);
            if (!Files.isReadable(docDir)) {
                System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
                System.exit(1);
            }

            try {
                System.out.println("Indexing to directory '" + index + "'...");
                Directory dir = FSDirectory.open(Paths.get(index));
//                String[] files= dir.listAll();
//                //Clean the index directory for the fresh index storage
//                for (String filename : files)
//                {
//                    File file = new File(index+"/"+filename);
//                    file.delete();
//                }
                /**
                 * Parsing of data sets in Data folder and indexing them as soon as they are parsed
                 * to reduce the memory load. Comment/Uncomment the lines up to "indexing complete"
                 * statement to remove the indexing. Indexing is now using Create_or_Append config.
                 */
                IndexConfig iconfig = new IndexConfig(dir);
                IndexWriter writer = new IndexWriter(iconfig.get_index_directory(), iconfig.get_index_configuration());
                Parser.listFilesForFolder(new File(docDir.toString()), writer, false);
                writer.close();
                System.out.println("-----------Indexing complete-----------");
                Searching.startSearching(index, queries, numdocs);
                Scoring.scoreit();
            } catch (IOException e) {
                System.out.println(" caught a " + e.getClass() +
                        "\n with message: " + e.getMessage());
            }
    }
}
