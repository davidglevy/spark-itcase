package levy.david.spark.itcase.core.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;

public class Booter
{

	private static final String REPO_LOC_DEFAULT = System.getProperty("user.home") + File.separator + ".spark_itcase/repo";
	
    public static RepositorySystem newRepositorySystem()
    {
        return ManualRepositorySystemFactory.newRepositorySystem();
    }

    public static DefaultRepositorySystemSession newRepositorySystemSession( RepositorySystem system, ServletOutTransferListener listener )
    {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository( REPO_LOC_DEFAULT );
        session.setLocalRepositoryManager( system.newLocalRepositoryManager( session, localRepo ) );

        if (listener == null) {
            session.setTransferListener( new ConsoleTransferListener() );
        } else {
        	session.setTransferListener(listener);
        }
        
        session.setRepositoryListener( new ConsoleRepositoryListener() );

        // uncomment to generate dirty trees
        // session.setDependencyGraphTransformer( null );

        return session;
    }

    public static List<RemoteRepository> newRepositories( RepositorySystem system, RepositorySystemSession session )
    {
        return new ArrayList<RemoteRepository>( Arrays.asList( newCentralRepository() ) );
    }

    private static RemoteRepository newCentralRepository()
    {
    	// TODO Read these from a settings.xml. Lookup default if present. Why is this not done as part of aether???
        return new RemoteRepository.Builder( "central", "default", "http://central.maven.org/maven2/" ).build();
    }

}