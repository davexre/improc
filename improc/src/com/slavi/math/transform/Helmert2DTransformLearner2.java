package com.slavi.math.transform;

import java.util.Map;

import com.slavi.math.MathUtil;
import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

/*

 TX' =  a * SX + b * SY + c
 TY' = -b * SX + a * SY + d

 dX = TX' - TX
 dY = TY' - TY

 Adjust a,b,c,d so that the distance F = Math.Sqrt(dX*dX + dY*dY) = min
 
 */
public abstract class Helmert2DTransformLearner2<InputType, OutputType> extends BaseTransformLearner<InputType, OutputType> {

	protected Matrix coefs;			// Temporary matrix, used by computeOne methods
	protected LeastSquaresAdjust lsa;

	public Helmert2DTransformLearner2(Helmert2DTransformer2<InputType, OutputType> transformer, 
			Iterable<? extends Map.Entry<InputType, OutputType>> pointsPairList) {
		super(transformer, pointsPairList);

		this.coefs = new Matrix(4, 1);
		this.lsa = new LeastSquaresAdjust(4, 1);
	}
	
	public int getRequiredTrainingPoints() {
		return 4;
	}	
	
	public TransformLearnerResult calculateTwo() {
		TransformLearnerResult result = new TransformLearnerResult();
		result.minGoodRequired = lsa.getRequiredPoints();
		computeWeights(result);
		if (result.oldGoodCount < result.minGoodRequired)
			return result;

		Helmert2DTransformer2<InputType, OutputType> tr = (Helmert2DTransformer2<InputType, OutputType>)transformer;
		// Calculate the affine transform parameters 
		lsa.clear();
		for (Map.Entry<InputType, OutputType> item : items) {
			if (isBad(item))
				continue;
			InputType source = item.getKey();
			OutputType target = item.getValue();
			double computedWeight = 1.0; //getComputedWeight(item);
			
			double SX = transformer.getSourceCoord(source, 0);
			double SY = transformer.getSourceCoord(source, 1);
			double TX = transformer.getTargetCoord(target, 0);
			double TY = transformer.getTargetCoord(target, 1);
			double L = tr.a * SX + tr.b * SY + tr.c - TX;
			
			System.out.println(L + "\t" + SX + "\t" + SY + "\t" + 1 + "\t" + 0);
			
			coefs.setItem(0, 0, SX);
			coefs.setItem(1, 0, SY);
			coefs.setItem(2, 0, 1);
			coefs.setItem(3, 0, 0);
			lsa.addMeasurement(coefs, computedWeight, L, 0);

			L = -tr.b * SX + tr.a * SY + tr.d - TY;
			
			System.out.println(L + "\t" + SY + "\t" + (-SX) + "\t" + 0 + "\t" + 1);
			
			coefs.setItem(0, 0, SY);
			coefs.setItem(1, 0, -SX);
			coefs.setItem(2, 0, 0);
			coefs.setItem(3, 0, 1);
			lsa.addMeasurement(coefs, computedWeight, L, 0);
		}
		System.out.println("det=" + lsa.getNm().makeSquareMatrix().det());
		Matrix nm = lsa.getNm().makeSquareMatrix();
		System.out.println("NM");
		nm.save(System.out);
//		nm.printM("NM");
		if (!lsa.calculate())
			return result;
		Matrix nm_inv = lsa.getNm().makeSquareMatrix();
		nm_inv.printM("NM'");
		Matrix check = new Matrix();
		nm.mMul(nm_inv, check);
		check.printM("nm*nm'");
		double sumAbs = check.sumAbs();
		System.out.println("sumAbs=" + sumAbs);
		if (Math.abs(sumAbs - check.getSizeX()) > 1)
			throw new RuntimeException("");
		
//		if (!lsa.calculate())
//			return result;
		
		// Build transformer
		Matrix u = lsa.getUnknown();
		u.rMul(-1);
		u.printM("U");
		
		tr.a += u.getItem(0, 0);
		tr.b += u.getItem(0, 1);
		tr.c += u.getItem(0, 2); 
		tr.d += u.getItem(0, 3);
		
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result; 
	}
	
	public TransformLearnerResult calculateOne() {
		TransformLearnerResult result = new TransformLearnerResult();
		result.minGoodRequired = lsa.getRequiredPoints();
		computeWeights(result);
		if (result.oldGoodCount < result.minGoodRequired)
			return result;
		Helmert2DTransformer2<InputType, OutputType> tr = (Helmert2DTransformer2<InputType, OutputType>)transformer;
		// Calculate the affine transform parameters 
		lsa.clear();

		Statistics stC = new Statistics();
		Statistics stD = new Statistics();
		stC.start();
		stD.start();
		
		for (Map.Entry<InputType, OutputType> item : items) {
			if (isBad(item))
				continue;
			InputType source = item.getKey();
			OutputType target = item.getValue();
			double computedWeight = 1.0; //getComputedWeight(item);
			
			double SX = transformer.getSourceCoord(source, 0);
			double SY = transformer.getSourceCoord(source, 1);
			double TX = transformer.getTargetCoord(target, 0);
			double TY = transformer.getTargetCoord(target, 1);

			double DX = tr.a * SX + tr.b * SY + tr.c - TX;
			double DY =-tr.b * SX + tr.a * SY + tr.d - TY;
			stC.addValue(DX);
			stD.addValue(DY);
			
			double F = DX*DX + DY*DY;
		}
		stC.stop();
		stD.stop();
		System.out.println("stC");
		System.out.println(stC);
		System.out.println("stD");
		System.out.println(stD);
		System.out.println("--");
		tr.c -= stC.getAvgValue();
		tr.d -= stD.getAvgValue();
		
		
		computeScaleAndOrigin();

		double aSourceScale = Math.max(sourceScale.getItem(0, 0), sourceScale.getItem(1, 0));
		double aTargetScale = Math.max(targetScale.getItem(0, 0), targetScale.getItem(1, 0));
		
		for (Map.Entry<InputType, OutputType> item : items) {
			if (isBad(item))
				continue;
			InputType source = item.getKey();
			OutputType target = item.getValue();
			double computedWeight = 1.0; //getComputedWeight(item);
			
//			double SX = (transformer.getSourceCoord(source, 0) - sourceOrigin.getItem(0, 0)) / sourceScale.getItem(0, 0);
//			double SY = (transformer.getSourceCoord(source, 1) - sourceOrigin.getItem(1, 0)) / sourceScale.getItem(1, 0);
//			double TX = (transformer.getTargetCoord(target, 0) - targetOrigin.getItem(0, 0)) / targetScale.getItem(0, 0);
//			double TY = (transformer.getTargetCoord(target, 1) - targetOrigin.getItem(1, 0)) / targetScale.getItem(1, 0);

			double SX = transformer.getSourceCoord(source, 0);
			double SY = transformer.getSourceCoord(source, 1);
			double TX = transformer.getTargetCoord(target, 0);
			double TY = transformer.getTargetCoord(target, 1);
			
			double DX = tr.a * SX + tr.b * SY + tr.c - TX;
			double DY =-tr.b * SX + tr.a * SY + tr.d - TY;
			double F = DX*DX + DY*DY;
			if (F == 0.0) {
				//throw new Error("0");
				System.out.println("Got F=0");
				continue;
			}
			double dF_da = DX * SX + DY * SY; 
			double dF_db = DX * SY - DY * SX; 
			double dF_dc = DX;
			double dF_dd = DY;
			
//			dF_da *= scaleX;
//			dF_db *= scaleY;
			
//			System.out.println(F + "\t" + dF_da + "\t" + dF_db + "\t" + dF_dc + "\t" + dF_dd);
			
			coefs.setItem(0, 0, dF_da);
			coefs.setItem(1, 0, dF_db);
			coefs.setItem(2, 0, dF_dc);
			coefs.setItem(3, 0, dF_dd);
			lsa.addMeasurement(coefs, computedWeight, F, 0);
		}
		System.out.println("det=" + lsa.getNm().makeSquareMatrix().det());
		Matrix nm = lsa.getNm().makeSquareMatrix();
		System.out.println("NM");
		nm.save(System.out);
//		nm.printM("NM");
		if (!lsa.calculate())
			return result;
		Matrix nm_inv = lsa.getNm().makeSquareMatrix();
		nm_inv.printM("NM'");
		Matrix check = new Matrix();
		nm.mMul(nm_inv, check);
		check.printM("nm*nm'");
		double sumAbs = check.sumAbs();
		System.out.println("sumAbs=" + sumAbs);
//		if (Math.abs(sumAbs - check.getSizeX()) > 1)
//			throw new RuntimeException("");

		// Build transformer
		Matrix u = lsa.getUnknown();
		u.rMul(-1);
		u.printM("U");
	
		tr.a += u.getItem(0, 0);
		tr.b += u.getItem(0, 1);
		tr.c += u.getItem(0, 2);
		tr.d += u.getItem(0, 3);
		
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result; 
	}
	
	public TransformLearnerResult calculateOne2() {
		TransformLearnerResult result = new TransformLearnerResult();
		result.minGoodRequired = lsa.getRequiredPoints();
		computeWeights(result);
		if (result.oldGoodCount < result.minGoodRequired)
			return result;
		Helmert2DTransformer2<InputType, OutputType> tr = (Helmert2DTransformer2<InputType, OutputType>)transformer;
		// Calculate the affine transform parameters 
		lsa.clear();
		computeScaleAndOrigin();
		
		double scaleX = 1.0; // / sourceScale.getItem(0, 0);
		double scaleY = 1.0; // / sourceScale.getItem(1, 0);
		
		for (Map.Entry<InputType, OutputType> item : items) {
			if (isBad(item))
				continue;
			InputType source = item.getKey();
			OutputType target = item.getValue();
			double computedWeight = 1.0; //getComputedWeight(item);
			
			double SX = transformer.getSourceCoord(source, 0);
			double SY = transformer.getSourceCoord(source, 1);
			double TX = transformer.getTargetCoord(target, 0);
			double TY = transformer.getTargetCoord(target, 1);

			double DX = tr.a * SX + tr.b * SY + tr.c - TX;
			double DY =-tr.b * SX + tr.a * SY + tr.d - TY;
			double F = DX*DX + DY*DY;
			if (F == 0.0) {
				//throw new Error("0");
				System.out.println("Got F=0");
				continue;
			}
			double dF_da = DX * SX + DY * SY; 
			double dF_db = DX * SY - DY * SX; 
			double dF_dc = DX;
			double dF_dd = DY;
			
			dF_da *= scaleX;
			dF_db *= scaleY;
			
			System.out.println(F + "\t" + dF_da + "\t" + dF_db + "\t" + dF_dc + "\t" + dF_dd);
			
			coefs.setItem(0, 0, dF_da);
			coefs.setItem(1, 0, dF_db);
			coefs.setItem(2, 0, dF_dc);
			coefs.setItem(3, 0, dF_dd);
			lsa.addMeasurement(coefs, computedWeight, F, 0);
		}
		System.out.println("det=" + lsa.getNm().makeSquareMatrix().det());
		Matrix nm = lsa.getNm().makeSquareMatrix();
		System.out.println("NM");
		nm.save(System.out);
//		nm.printM("NM");
		if (!lsa.calculate())
			return result;
		Matrix nm_inv = lsa.getNm().makeSquareMatrix();
		nm_inv.printM("NM'");
		Matrix check = new Matrix();
		nm.mMul(nm_inv, check);
		check.printM("nm*nm'");
		double sumAbs = check.sumAbs();
		System.out.println("sumAbs=" + sumAbs);
//		if (Math.abs(sumAbs - check.getSizeX()) > 1)
//			throw new RuntimeException("");

		// Build transformer
		Matrix u = lsa.getUnknown();
		u.rMul(-1);
		u.printM("U");
	
		tr.a += u.getItem(0, 0) / scaleX;
		tr.b += u.getItem(0, 1) / scaleY;
		tr.c += u.getItem(0, 2);
		tr.d += u.getItem(0, 3);
		
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result; 
	}
	
	public void testDerivative(double a, double b, double c, double d) {
		Helmert2DTransformer2<InputType, OutputType> tr = (Helmert2DTransformer2<InputType, OutputType>)transformer;
		for (Map.Entry<InputType, OutputType> item : items) {
			InputType source = item.getKey();
			OutputType target = item.getValue();

			double SX = transformer.getSourceCoord(source, 0);
			double SY = transformer.getSourceCoord(source, 1);
			double TX = transformer.getTargetCoord(target, 0);
			double TY = transformer.getTargetCoord(target, 1);
			
			double DX0 = a * SX + b * SY + c - TX;
			double DY0 =-b * SX + a * SY + d - TY;
			double F0 = DX0*DX0 + DY0*DY0;
						
			double DX = tr.a * SX + tr.b * SY + tr.c - TX;
			double DY =-tr.b * SX + tr.a * SY + tr.d - TY;
			double F = DX*DX + DY*DY;

			double dF_da = DX * SX + DY * SY; 
			double dF_db = DX * SY - DY * SX; 
			double dF_dc = DX;
			double dF_dd = DY;
			
			double F1 = F 
				+ dF_da * (a - tr.a)
				+ dF_db * (b - tr.b)
				+ dF_dc * (c - tr.c)
				+ dF_dd * (d - tr.d);
			System.out.println(isBad(item) + "\t" + MathUtil.d20(F1 - F0)
				+ "\t" + MathUtil.d20(Math.sqrt(F))
				+ "\t" + MathUtil.d20(Math.sqrt(F0)) );
		}		
	}	
}
