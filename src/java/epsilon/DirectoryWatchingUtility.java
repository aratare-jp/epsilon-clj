package epsilon;

import clojure.lang.IFn;
import io.methvin.watcher.DirectoryWatcher;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Path;

public class DirectoryWatchingUtility {
	/**
	 * Build a watcher of a directory for file changes. Accept a bunch of functions to be called when a file change is
	 * detected.
	 *
	 * @param filterFn      filtering out which file to skip and which file to process
	 * @param createHandler called when a file is created
	 * @param modifyHandler called when a file is modified
	 * @param deleteHandler called when a file is deleted
	 * @return a watcher that needs to be closed when done.
	 * @throws IOException when there's a problem with building the watcher
	 */
	public static DirectoryWatcher watch(Path directoryToWatch,
										 IFn filterFn,
										 IFn createHandler,
										 IFn modifyHandler,
										 IFn deleteHandler) throws IOException {
		return DirectoryWatcher.builder()
				.path(directoryToWatch) // or use paths(directoriesToWatch)
				.listener(event -> {
					if (!(Boolean) filterFn.invoke(event.path().toFile())) {
						return;
					}
					switch (event.eventType()) {
						case CREATE:
							createHandler.invoke(event.path().toFile());
							break;
						case MODIFY:
							modifyHandler.invoke(event.path().toFile());
							break;
						case DELETE:
							deleteHandler.invoke(event.path().toFile());
							break;
						default:
							break;
					}
				})
				.build();
	}
}