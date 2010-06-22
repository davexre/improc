package com.slavi.math.transform;

import java.util.Map;

import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.matrix.Matrix;

/*

 TX' = a * SX - b * SY + c
 TY' = b * SX + a * SY + d

 dX = TX' - TX
 dY = TY' - TY

 Adjust a,b,c,d so that the distance F = Math.Sqrt(dX*dX + dY*dY) = min
 
 */
public abstract class Helmert2DTransformLearner2<InputType, OutputType> extends BaseTransformLearner<InputType, OutputType> {

	protected Matrix coefs;			// Temporary matrix, used by computeOne methods
	protected LeastSquaresAdjust lsa;

	public Helmert2DTransformLearner2(Helmert2DTransformer<InputType, OutputType> transformer, 
			Iterable<? extends Map.Entry<InputType, OutputType>> pointsPairList) {
		super(transformer, pointsPairList);

		this.coefs = new Matrix(4, 1);
		this.lsa = new LeastSquaresAdjust(4, 1);
	}
	
	public int getRequiredTrainingPoints() {
		return 2;
	}
	
	public TransformLearnerResult calculateOne() {
		TransformLearnerResult result = new TransformLearnerResult();
		result.minGoodRequired = lsa.getRequiredPoints();
		computeWeights(result);
		
		if (result.oldGoodCount < result.minGoodRequired)
			return result;

		Helmert2DTransformer<InputType, OutputType> tr = (Helmert2DTransformer<InputType, OutputType>)transformer;
		// Calculate the affine transform parameters 
		lsa.clear();
		for (Map.Entry<InputType, OutputType> item : items) {
			if (isBad(item))
				continue;
			InputType source = item.getKey();
			OutputType target = item.getValue();
			double computedWeight = getComputedWeight(item);
			
			double SX = transformer.getSourceCoord(source, 0);
			double SY = transformer.getSourceCoord(source, 1);
			double TX = transformer.getTargetCoord(target, 0);
			double TY = transformer.getTargetCoord(target, 1);

			double L = tr.a * SX - tr.b * SY + tr.c - TX;
			coefs.setItem(0, 0, SX);
			coefs.setItem(1, 0, -SY);
			coefs.setItem(2, 0, 1);
			coefs.setItem(3, 0, 0);
			lsa.addMeasurement(coefs, computedWeight, L, 0);

			L = tr.b * SX + tr.a * SY + tr.d - TY;
			coefs.setItem(0, 0, SY);
			coefs.setItem(1, 0, SX);
			coefs.setItem(2, 0, 0);
			coefs.setItem(3, 0, 1);
			lsa.addMeasurement(coefs, computedWeight, L, 0);
		}
		if (!lsa.calculate())
			return result;
		
		// Build transformer
		Matrix u = lsa.getUnknown();
		tr.a -= u.getItem(0, 0);
		tr.b -= u.getItem(0, 1);
		tr.c -= u.getItem(0, 2); 
		tr.d -= u.getItem(0, 3);
		
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result; 
	}
}
