/*
 * This software is licensed under the terms of the GNU GENERAL PUBLIC LICENSE
 * Version 2, which can be found at http://www.gnu.org/copyleft/gpl.html
 */
package org.cubictest.export.converters;

import org.cubictest.export.holders.IResultHolder;
import org.cubictest.model.ICustomTestStepHolder;
import org.cubictest.model.customstep.data.CustomTestStepData;

public interface ICustomTestStepConverter<T extends IResultHolder> {
	public void handleCustomStep(T t, ICustomTestStepHolder cts, CustomTestStepData data);

	public String getDataKey();
}
