package com.slavi.math.transform2;

import java.util.Map;

import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;

public abstract class Helmert2DTransformLearner<InputType, OutputType> extends BaseTransformLearner<InputType, OutputType> {

	protected Matrix coefs;			// Temporary matrix, used by computeOne methods
	protected LeastSquaresAdjust lsa;

	public Helmert2DTransformLearner(Helmert2DTransformer<InputType, OutputType> transformer, 
			Iterable<Map.Entry<InputType, OutputType>> pointsPairList) {
		super(transformer, pointsPairList);

		this.coefs = new Matrix(4, 1);
		this.lsa = new LeastSquaresAdjust(4, 1);		
	}
	
	public int getRequiredTrainingPoints() {
		return 2;
	}	

	/**
	 * 
	 * @return True if adjusted. False - Try again/more adjustments needed.
	 */
	public boolean calculateOne() {
		int goodCount = computeWeights();

		if (goodCount < lsa.getRequiredPoints())
			throw new ArithmeticException("Not enough good points");

		computeScaleAndOrigin();
		double aSourceScale = Math.max(sourceScale.getItem(0, 0), sourceScale.getItem(1, 0));
		double aTargetScale = Math.max(targetScale.getItem(0, 0), targetScale.getItem(1, 0));
				
		aSourceScale = 1.0;
		aTargetScale = 1.0;
		sourceOrigin.make0();
		targetOrigin.make0();
		
		// Calculate the affine transform parameters 
		lsa.clear();
		for (Map.Entry<InputType, OutputType> item : items) {
			if (isBad(item))
				continue;

			InputType source = item.getKey();
			OutputType dest = item.getValue();
			double a = (transformer.getSourceCoord(source, 0) - sourceOrigin.getItem(0, 0)) / aSourceScale;
			double b = (transformer.getSourceCoord(source, 1) - sourceOrigin.getItem(1, 0)) / aSourceScale;
			double L;
			double computedWeight = getComputedWeight(item);
			
			L = (transformer.getTargetCoord(dest, 0) - targetOrigin.getItem(0, 0)) / aTargetScale;
			coefs.setItem(0, 0, a);
			coefs.setItem(1, 0, -b);
			coefs.setItem(2, 0, 1.0);
			coefs.setItem(3, 0, 0.0);
			lsa.addMeasurement(coefs, computedWeight, L, 0);
			
			L = (transformer.getTargetCoord(dest, 1) - targetOrigin.getItem(1, 0)) / aTargetScale;
			coefs.setItem(0, 0, b);
			coefs.setItem(1, 0, a);
			coefs.setItem(2, 0, 0.0);
			coefs.setItem(3, 0, 1.0);
			lsa.addMeasurement(coefs, computedWeight, L, 0);
		}
		lsa.calculate();

		// Build transformer
		Helmert2DTransformer tr = (Helmert2DTransformer)transformer;
		Matrix u = lsa.getUnknown(); 

		tr.a = u.getItem(0, 0) * aTargetScale / aSourceScale;
		tr.b = u.getItem(0, 1) * aTargetScale / aSourceScale;
		tr.c = u.getItem(0, 2) * aTargetScale - targetOrigin.getItem(0, 0) - 
				tr.a * sourceOrigin.getItem(0, 0) - tr.b * sourceOrigin.getItem(1, 0); 
		tr.d = u.getItem(0, 3) * aTargetScale - targetOrigin.getItem(1, 0) + 
				tr.b * sourceOrigin.getItem(0, 0) - tr.a * sourceOrigin.getItem(1, 0); 
		
		return isAdjusted();
	}
}
