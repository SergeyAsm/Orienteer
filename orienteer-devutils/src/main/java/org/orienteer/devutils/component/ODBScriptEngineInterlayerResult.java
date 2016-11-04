package org.orienteer.devutils.component;

import ru.ydn.wicket.wicketconsole.AbstractScriptEngineInterlayerResult;

/**
 * Result object for {@link ODBScriptEngineInterlayer}
 *
 */
public class ODBScriptEngineInterlayerResult extends AbstractScriptEngineInterlayerResult{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void onUpdate() {
		Object ret = getReturnedObject();
		if (ret!=null){
			setOut(ret.toString());
		}
	}
}
