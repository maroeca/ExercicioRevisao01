package br.pucpr.mage;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Classe para trabalhar com shaders.
 */
public class Shader {
    /**
     * Le o conteúdo de um arquivo e carrega em uma String
     * @param is Um InputStream apontando para o arquivo
     * @return Um texto com o conteúdo do arquivo
     */
    private static String readInputStream(InputStream is) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))){
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException("Unable to load shader", e);
        }
    }

    /**
     * Compila o código do shader.
     *
     * @param type Tipo do shader a ser compilado. Pode ser GL_VERTEX_SHADER, GL_FRAGMENT_SHADER ou GL_GEOMETRY_SHADER.
     * @param code Código fonte do shader
     * @return O id do shader compilado
     * @throws RuntimeException Caso o código contenha erros.
     */
    private static int compileShader(int type, String code) {
        int shader = glCreateShader(type);
        glShaderSource(shader, code);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String typeStr = type == GL_VERTEX_SHADER ? "vertex" : type == GL_FRAGMENT_SHADER ? "fragment" : "geometry";
            throw new RuntimeException("Unable to compile " + typeStr + " shader." + glGetShaderInfoLog(shader));
        }
        return shader;
    }

    /**
     * Carrega e compila o shader indicado no parâmetro.
     * @param name Nome do shader a ser carregado
     * @return O shader compilado.
     */
    private static int loadShader(String name) {
        name = "/br/pucpr/resource/" + name.toLowerCase();

        //Associa o tipo do shader de acordo com a extensão
        int type;
        if (name.endsWith(".vert") || name.endsWith(".vs"))
            type = GL_VERTEX_SHADER;
        else if (name.endsWith(".frag") || name.endsWith(".fs"))
            type = GL_FRAGMENT_SHADER;
        else if (name.endsWith(".geom") || name.endsWith(".gs"))
            type = GL_GEOMETRY_SHADER;
        else throw new IllegalArgumentException("Invalid shader name: " + name);

        //Carrega o shader do disco
        String code = readInputStream(Shader.class.getResourceAsStream(name));

        //Compila o shader
        return compileShader(type, code);
    }

    /**
     * Une um vertex e um fragment shader, gerando o shader program que será usado no desenho.
     * O parâmetro de entrada dessa função é um array com o id de todos os shaders que devem ser unidos.
     * @param shaders Ids dos shaders a serem linkados
     * @return id do programa gerado
     * @throws RuntimeException Caso algum erro de link ocorra.
     */
    private static int linkProgram(int... shaders) {
        int program = glCreateProgram();
        for (int shader : shaders) {
            glAttachShader(program, shader);
        }

        glLinkProgram(program);
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Unable to link shaders." + glGetProgramInfoLog(program));
        }

        for (int shader : shaders) {
            glDetachShader(program, shader);
            glDeleteShader(shader);
        }

        return program;
    }

    /**
     * Carrega o shader program formado pelos shaders indicados
     * @param shaders Shaders para carregar
     * @return O id do shader program
     * @throws RuntimeException Caso um erro de compilação ou link ocorra.
     */
    public static int loadProgram(String ...shaders) {
        int[] ids = new int[shaders.length];
        for (int i = 0; i < shaders.length; i++) {
            ids[i] = loadShader(shaders[i]);
        }
        return linkProgram(ids);
    }
}
