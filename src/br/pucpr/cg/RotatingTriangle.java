package br.pucpr.cg;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import br.pucpr.mage.Keyboard;
import br.pucpr.mage.Scene;
import br.pucpr.mage.Shader;
import br.pucpr.mage.Window;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

/**
 * Essa classe demonstra como desenhar um triangulo na tela utilizando a OpenGL.
 */
public class RotatingTriangle implements Scene {



	private Keyboard keys = Keyboard.getInstance();

	/** Esta variável guarda o identificador da malha (Vertex Array Object) do triângulo */
	private int vao;


	/** Guarda o id do buffer com todas as posições do vértice. */
	private int positions;

	/** Guarda o id do buffer com todas as cores do vértice */
	private int colors;

	private int indexBuffer;


	/** Guarda o id do shader program, após compilado e linkado */
	private int shader;

	/** Angulo que o triangulo está */
	private float angle;

	int criaBuffer(float... data)
	{
		int aux;
		//Solicitamos a criação de um buffer na OpenGL, onde esse array será guardado
		aux = glGenBuffers();
		//Informamos a OpenGL que iremos trabalhar com esse buffer
		glBindVertexArray(aux);
		//Damos o comando para carregar esses dados na placa de vídeo
		glBindBuffer(GL_ARRAY_BUFFER, aux);

		//Damos o comando para carregar esses dados na placa de vídeo
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);

		return aux;
	}

	int criaIndexBuffer(int... data)
	{
		int aux;
		//Solicitamos a criação de um buffer na OpenGL, onde esse array será guardado
		aux = glGenBuffers();
		//Informamos a OpenGL que iremos trabalhar com esse buffer
		glBindVertexArray(aux);
		//Damos o comando para carregar esses dados na placa de vídeo
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, aux);

		//Damos o comando para carregar esses dados na placa de vídeo
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, data, GL_STATIC_DRAW);

		return aux;

	}

	int associateBuffer(int id, int size, String name)
	{
		//Procuramos o identificador do atributo de posição
		int buff = glGetAttribLocation(shader, name);

		//Informamos a OpenGL que iremos trabalhar com essa variável
		glEnableVertexAttribArray(buff);

		//Informamos ao OpenGL que também trabalharemos com o buffer de posições
		glBindBuffer(GL_ARRAY_BUFFER, id);

		//Chamamos uma função que associa as duas.
		glVertexAttribPointer(buff, size, GL_FLOAT, false, 0, 0);
		return buff;
	}

	@Override
	public void init() {
		//Define a cor de limpeza da tela
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		//------------------
		//Criação da malha
		//------------------

		//O processo de criação da malha envolve criar um Vertex Array Object e associar a ele um buffer, com as
		// posições dos vértices do triangulo.

		//Criação do Vertex Array Object (VAO)
		vao = glGenVertexArrays();

		//Informamos a OpenGL que iremos trabalhar com esse VAO
		glBindVertexArray(vao);


		//Criação do buffer de posições
		//------------------------------
		float[] vertexData = new float[] {
				-0.5f,  0.5f,   //Vertice 0
				0.5f,  0.5f,   //Vertice 1
				-0.5f, -0.5f,   //Vertice 2
				0.5f, -0.5f    //Vertice 3

		};

		positions = criaBuffer(vertexData);

		//Como já finalizamos a carga, informamos a OpenGL que não estamos mais usando esse buffer.
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		//Criação do buffer de cores
		//------------------------------
		float[] colorData = new float[] {
				1.0f, 0.0f, 0.0f, //Vertice 0
				1.0f, 1.0f, 0.0f, //Vertice 1
				0.0f, 1.0f, 0.0f, //Vertice 2
				0.0f, 0.0f, 1.0f  //Vertice 3
		};

		colors = criaBuffer(colorData);

		//Como já finalizamos a carga, informamos a OpenGL que não estamos mais usando esse buffer.
		glBindBuffer(GL_ARRAY_BUFFER, 0);


		//Finalizamos o nosso VAO, portanto, informamos a OpenGL que não iremos mais trabalhar com ele
		glBindVertexArray(0);

		int[] indices = {
				0, 3 ,2,
				0, 3, 1	};

		 indexBuffer = criaIndexBuffer(indices);



		//------------------------------
		//Carga/Compilação dos shaders
		//------------------------------

		shader = Shader.loadProgram("basic.vert", "basic.frag");
	}

	@Override
	public void update(float secs) {
		//Testa se a tecla ESC foi pressionada
		if (keys.isPressed(GLFW_KEY_ESCAPE)) {
			//Fecha a janela, caso tenha sido
			glfwSetWindowShouldClose(glfwGetCurrentContext(), true);
			return;
		}

		//Somamos alguns graus de modo que o angulo mude 180 graus por segundo
		//angle += Math.toRadians(180) * secs;
	}

	@Override
	public void draw() {
		//Solicita a limpeza da tela
		glClear(GL_COLOR_BUFFER_BIT);

		//Precisamos dizer qual VAO iremos desenhar
		glBindVertexArray(vao);

		//E qual shader program irá ser usado durante o desenho
		glUseProgram(shader);


		//Associação da variável World ao shader
		//--------------------------------------
		//Criamos um objeto da classe FloatBuffer
		FloatBuffer transform = BufferUtils.createFloatBuffer(16);

		//Criamos uma matriz de rotação e a enviamos para o buffer transform
		new Matrix4f().rotateY(angle).get(transform);

		//Procuramos pelo id da variável uWorld, dentro do shader
		int uWorld = glGetUniformLocation(shader, "uWorld");

		// Copiamos os dados do buffer para a variável que está no shader
		glUniformMatrix4fv(uWorld, false, transform);


		//Associação do buffer positions a variável aPosition
		//---------------------------------------------------
		//Procuramos o identificador do atributo de posição
		int aPosition = associateBuffer(positions, 2, "aPosition");


		//Associação do buffer cores a variável aColor
		//---------------------------------------------------
		//Procuramos o identificador do atributo de posição
		int aColor = associateBuffer(colors, 3, "aColor");


		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
		//Comandamos o desenho de 3 vértices
		glDrawElements(GL_TRIANGLES, 6,GL_UNSIGNED_INT, 0);


		//Faxina
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDisableVertexAttribArray(aPosition);
        glDisableVertexAttribArray(aColor);
		glBindVertexArray(0);
		glUseProgram(0);
	}

	@Override
	public void deinit() {
	}

	public static void main(String[] args) {
		new Window(new RotatingTriangle()).show();
	}
}