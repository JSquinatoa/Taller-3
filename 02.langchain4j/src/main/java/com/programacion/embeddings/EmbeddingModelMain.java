package com.programacion.embeddings;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.PoolingMode;
import dev.langchain4j.model.output.Response;

import java.nio.file.Paths;

public class EmbeddingModelMain {

    static void main(String[] args) throws Exception {

        var pathToModel      = Paths.get("C:/tools/llama-models/model.onnx");
        var pathToTokenizer  = Paths.get("C:/tools/llama-models/tokenizer.json");

//        OnnxEmbeddingModel model = new OnnxEmbeddingModel(
//                pathToModel, // path al ONNX
//                pathToTokenizer, // Path al tokenizer
//                PoolingMode.MEAN // este de aqui hace lo siguiente
//                /*
//                * esto lo descomponet en tokens que son los sigueinte
//                * 1.Hola
//                * 2. ,
//                * 3. como
//                * 4. estas
//                * 5. ?
//                *
//                * De donde saca los ids del tokenizer y asocia un id con el token
//                * con ese tokenId nos genera un vector para cada palabra
//                * la dimención depende del modelo
//                * para este token va a ahcer un vector y va a tener 5 vectores,
//                * des esos vectores ssaca la emdia,y ese es el embedding de la frase, y va a tener 5
//                * vectore sy de cada uno va a ser la media de los 5 y me va a dejar un tamalo por ejemplo de 512
//                * eso va a ser el mebdding de la frase de ahi
//                * por eso es importante el Poolling.MEAN
//                *
//                *
//                * */
//        );

        // con este no hace falta descar el modelo,

        AllMiniLmL6V2EmbeddingModel model = new AllMiniLmL6V2EmbeddingModel();

        Response<Embedding> response = model.embed("Hola, cómo estás?");

        Embedding embedding = response.content();

        System.out.println("Dimensio : " + embedding.vector().length); //384
        System.out.println(embedding.vectorAsList());


    }
}
