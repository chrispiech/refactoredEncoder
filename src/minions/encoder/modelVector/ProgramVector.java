package minions.encoder.modelVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.encoder.ClusterableMatrix;
import models.encoder.EncoderParams;
import models.encoder.ModelFormat;
import models.encoder.encoders.Encoder;
import models.encoder.encoders.InternalEncoder;
import models.encoder.encoders.programEncoder.ProgramEncoder;
import models.encoder.encoders.programEncoder.ProgramEncoderMatrix;
import models.encoder.encoders.programEncoder.ProgramEncoderVec;

import org.ejml.simple.SimpleMatrix;

import util.MatrixUtil;
import util.Warnings;

public class ProgramVector {

	public static ProgramEncoderVec vecToProgram(ModelFormat format,
			List<Double> list) {
		int split1 = format.getInternalDimension();
		int split2 = split1 + format.getLeafDimension();
		Warnings.check(split2 == list.size());
		List<Double> internalList = list.subList(0, split1);
		List<Double> leavesList = list.subList(split1, split2);

		int N = EncoderParams.getN();
		HashMap<String, InternalEncoder> internalEncoders = 
				new HashMap<String, InternalEncoder>();
		for(String type : format.getInternalEncoderTypes()) {
			int dim = format.getInternalEncoderDimension(type);
			List<Double> encoderList = internalList.subList(0, dim);
			internalList = ModelVector.listPop(internalList, dim);
			int arity = format.getArity(type);
			List<SimpleMatrix> Ws = new ArrayList<SimpleMatrix>();
			for(int i = 0; i < arity; i++) {
				int size = N * N;
				List<Double> wList = encoderList.subList(0, size);
				encoderList = ModelVector.listPop(encoderList, size);
				SimpleMatrix W = MatrixUtil.listToMatrix(wList, N, N);
				Ws.add(W);
			}
			SimpleMatrix b = MatrixUtil.listToMatrix(encoderList, N, 1);

			InternalEncoder encoder = new InternalEncoder(type, Ws, b);
			internalEncoders.put(type, encoder);
		}

		// extract leaves
		HashMap<String, SimpleMatrix> leaves = new HashMap<String, SimpleMatrix>();
		for(String type : format.getLeafTypes()) {
			List<Double> leafList = leavesList.subList(0, N);
			leavesList = ModelVector.listPop(leavesList, N);
			SimpleMatrix v = MatrixUtil.listToMatrix(leafList, N, 1);
			leaves.put(type, new SimpleMatrix(v));
		}
		


		return new ProgramEncoderVec(format, internalEncoders, leaves);
	}
	
	public static ProgramEncoderMatrix vecToProgramMatrix(ModelFormat format,
			List<Double> list) {
		int split1 = format.getInternalDimension();
		int split2 = split1 + format.getLeafDimension();
		Warnings.check(split2 == list.size());
		List<Double> internalList = list.subList(0, split1);
		List<Double> leavesList = list.subList(split1, split2);

		int M = EncoderParams.getStateVectorSize();
		int N = EncoderParams.getCodeVectorSize();
		HashMap<String, InternalEncoder> internalEncoders = 
				new HashMap<String, InternalEncoder>();
		for(String type : format.getInternalEncoderTypes()) {
			int dim = format.getInternalEncoderDimension(type);
			List<Double> encoderList = internalList.subList(0, dim);
			internalList = ModelVector.listPop(internalList, dim);
			int arity = format.getArity(type);
			List<SimpleMatrix> Ws = new ArrayList<SimpleMatrix>();
			for(int i = 0; i < arity; i++) {
				List<Double> wList = encoderList.subList(0, N);
				encoderList = ModelVector.listPop(encoderList, N);
				SimpleMatrix W = MatrixUtil.listToMatrix(wList, M, M);
				Ws.add(W);
			}
			SimpleMatrix b = MatrixUtil.listToMatrix(encoderList, M, M);

			InternalEncoder encoder = new InternalEncoder(type, Ws, b);
			internalEncoders.put(type, encoder);
		}

		// extract leaves
		HashMap<String, SimpleMatrix> leaves = new HashMap<String, SimpleMatrix>();
		for(String type : format.getLeafTypes()) {
			List<Double> leafList = leavesList.subList(0, N);
			leavesList = ModelVector.listPop(leavesList, N);
			SimpleMatrix v = MatrixUtil.listToMatrix(leafList, M, M);
			leaves.put(type, new SimpleMatrix(v));
		}
		


		return new ProgramEncoderMatrix(format, internalEncoders, leaves);
	}
	
	public static String getNameForIndex(ModelFormat f, int i) {
		int internalSize = f.getInternalDimension();
		int programVecSize = internalSize + f.getLeafDimension();
		
		if(i < internalSize) {
			return "internal";
		} else if (i < programVecSize) {
			int leafIndex = i - internalSize;
			List<String> types = f.getLeafTypes();
			int N = EncoderParams.getN();
			int typeIndex = leafIndex / N;
			return types.get(typeIndex);
		} else {
			throw new RuntimeException("index out of bounds");
		}
		
	}
	
	public static List<Double> programToVec(Encoder model) {
		ProgramEncoder programEncoder = model.getProgramEncoder();
		List<Double> vecList = new ArrayList<Double>();
		
		// add internal encoders
		for(String type : model.getFormat().getInternalEncoderTypes()) {
			InternalEncoder encoder = programEncoder.getInternalEncoder(type);
			for(int i = 0; i < encoder.getArity(); i++) {
				vecList.addAll(MatrixUtil.matrixToList(encoder.getW(i)));
			}
			vecList.addAll(MatrixUtil.matrixToList(encoder.getB()));
		}
		
		// add leaf
		for(String type : model.getFormat().getLeafTypes()) {
			SimpleMatrix v = programEncoder.getLeafEmbedding(type);
			vecList.addAll(MatrixUtil.matrixToList(v));
		}  
		
		
		
		return vecList;
	}


}
