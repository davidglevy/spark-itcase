package levy.david.spark.itcase.core;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

public class LocalRepositoryTest {

	@Test
	public void testRetrieve() {
		LocalRepository repo = new LocalRepository();
		repo.initialize();
		
		repo.retrieveArtifact("xerces", "xercesImpl", "2.8.0", new ByteArrayOutputStream());
	}
	
}
