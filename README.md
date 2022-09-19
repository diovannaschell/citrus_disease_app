# Detecção de doenças em folhas de citros

Este projeto foi desenvolvido por Diovanna Schell, como trabalho de conclusão do curso de engenharia da computação.

O APP é uma ferramenta de auxilio na detecção de doenças em frutas cítricas através da análise de imagens de folhas sintomáticas. 

O APP foi desenvolvido na IDE Android Studio usando a linguagem de programação Java. Um modelo de IA foi treinado usando o *dataset* [Citrus Leaves](https://www.tensorflow.org/datasets/catalog/citrus_leaves) e posteriormente foi exportado usando o [TensorFlow Lite](https://www.tensorflow.org/?gclid=CjwKCAjwpqCZBhAbEiwAa7pXea7uSAZywNue8_aFsEZmReAxrYr6APxbuoppfqxAY7w_796gSCHvRBoCP5kQAvD_BwE). Para um aprimoramento nas análises, a biblioteca OpenCv foi utilizada para aplicar um [filtro de suavização gaussiana](https://docs.opencv.org/4.x/d4/d13/tutorial_py_filtering.html) nas imagens do dataset e também nas que são selecionadas pelo usuário no APP, removendo assim possíveis ruídos.

Para rodar o APP basta clonar o projeto e abrir ele com o Android Studio.
