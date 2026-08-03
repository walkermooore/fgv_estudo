package com.fgv.studyhub.vector;
import com.fgv.studyhub.entity.StudyChunk;
import java.util.List;
public interface VectorStore { void index(StudyChunk chunk,List<Double> embedding); List<VectorMatch> search(List<Double> embedding,int limit,Long materialId); }
