package epsilon;

import org.eclipse.epsilon.egl.EglFileGeneratingTemplateFactory;
import org.eclipse.epsilon.egl.EglTemplate;
import org.eclipse.epsilon.egl.exceptions.EglRuntimeException;
import org.eclipse.epsilon.egl.spec.EglTemplateSpecification;
import org.eclipse.epsilon.eol.dom.Operation;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomEglFileGeneratingTemplateFactory extends EglFileGeneratingTemplateFactory {
	private List<Operation> ops = new ArrayList<>();

	public void setOutputRoot(String path) throws EglRuntimeException {
		File outputRoot = new File(path);
		if (!outputRoot.exists()) {
			outputRoot.mkdirs();
		} else if (!outputRoot.isDirectory()) {
			throw new EglRuntimeException(new EolRuntimeException("Output path must be a directory"));
		}
		this.outputRootPath = outputRoot.getAbsolutePath();
		this.outputRoot = outputRoot.toURI();
	}

	public void addOperations(Collection<Operation> ops) {
		this.ops.addAll(ops);
	}

	@Override
	protected EglTemplate createTemplate(EglTemplateSpecification spec) throws Exception {
		EglTemplate template = super.createTemplate(spec);
		template.getOperations().addAll(ops);
		return template;
	}
}