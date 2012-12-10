package no.roek.nlpgraphs.preprocessing;


public class ParseTest {

//TODO: takes a couple seconds, uncommit to test dependency parsing
//	@Test
//	public void shouldDependencyParse() throws ClassNotFoundException, IOException, NullPointerException, MaltChainedException {
//		String filename = "parse_test.txt";
//		String dir = "src/test/resources/documents/";
////		List<NLPSentence> sentences = SentenceUtils.getSentences(dir+filename);
//		POSTagParser parser = new POSTagParser();
//		
//		ParseJob job = parser.posTagFile(Paths.get(dir+filename));
//		
//		ConfigService cs = new ConfigService();
//		MaltParserService maltService = new MaltParserService();
//		maltService.initializeParserModel(cs.getMaltParams());
//		String outDir = "src/test/resources/parsetest/";
//		
//		DependencyParser dependencyParser = new DependencyParser();
//		dependencyParser.dependencyParse(job, outDir);
////		ParseUtils.dependencyParse(job, outDir, maltService);
//		
//		Graph graph = GraphUtils.getGraphFromFile(outDir+job.getParentDir()+job.getFilename(), 1);
//		
//		Node node = graph.getNode(1);
//		assertEquals("be", node.getAttributes().get(0));
//		assertEquals("VBZ", node.getAttributes().get(1));
//	}
}
