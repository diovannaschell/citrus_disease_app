# Detecção de doenças em folhas de citros

Este projeto foi desenvolvido por Diovanna Schell, como trabalho de conclusão do curso de engenharia da computação.

O APP é uma ferramenta de auxilio na detecção de doenças em frutas cítricas através da análise de imagens de folhas sintomáticas. 

O APP foi desenvolvido na IDE Android Studio usando a linguagem de programação Java. Um modelo de IA foi treinado usando o *dataset* [Citrus Leaves](https://www.tensorflow.org/datasets/catalog/citrus_leaves) e posteriormente foi exportado usando o [TensorFlow Lite](https://www.tensorflow.org/?gclid=CjwKCAjwpqCZBhAbEiwAa7pXea7uSAZywNue8_aFsEZmReAxrYr6APxbuoppfqxAY7w_796gSCHvRBoCP5kQAvD_BwE) para poder ser usado no APP. Para um aprimoramento nas análises, a biblioteca OpenCv foi utilizada para aplicar um [filtro de suavização gaussiana](https://docs.opencv.org/4.x/d4/d13/tutorial_py_filtering.html) nas imagens do dataset e também nas que são selecionadas pelo usuário no APP, removendo assim possíveis ruídos.

Para rodar o APP basta clonar o projeto e abrir ele com o Android Studio.

O APP desenvolvido está disponível para download na [Play Store](https://play.google.com/store/apps/details?id=com.aplicativo.diseasedetector).

O código usado para o treinamento e exportação do modelo está disponível no link: [Treinamento da IA](https://colab.research.google.com/drive/143chAnQ5uRMnSptG9kAKDeB4D8vzNRgO?usp=sharing). Para o treinamento do modelo, usou-se 80% das imagens para treino, 10% para teste e 10% para validação.

O código usado para a validação do modelo na amostra da pesquisa está disponível neste link: [Validação da IA](https://colab.research.google.com/drive/1Oduqy8WTo6e4c-Cra2FNQLNq58E4X0pz?usp=sharing). A população estudada contava com 928 imagens, por tanto para atingir um grau de confiança de 95% com 5% de margem de erro, utilizou-se uma amostra de no mínimo 272 imagens.
