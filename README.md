# Introduction 
Microservisio encargado de obtener las cifras Casuistica para PMC 

#Variables de entorno
============== AMBIENTE DE QA ==============

Para que este microservicio pueda ser ejecutado deberán de existir las siguietnes variables de entorno en el SO annfitrión donde se ejecute el jar
- portMSReportes 8049 
- authenticationdatabaseMongo PMCQA01
- usrMongoCambios pmcmsconsulta
- pwdMongoCambios pmcmsconsulta0
- databaseMongo pmc_qa
- portMongo 27017
- hostMongo 10.100.8.78
- fileLogReportes /weblogic/pmc/logs/mspmc-reportes.log

- fileReportes /pmcarchivos/cierre_anual
- fileDictamen /pmcarchivos/dictamen
- sftp true
- sftpHost 10.100.6.76
- sftpPassword 	PMC123
- sftpPort 22
- sftpUser usr_pmc

============== AMBIENTE DE UAT ==============

Para que este microservicio pueda ser ejecutado deberán de existir las siguietnes variables de entorno en el SO annfitrión donde se ejecute el jar
- portMSCifrasCausitica 8049
- authenticationdatabaseMongo PMCUAT01
- usrMongoCambios pmcmsconsulta
- pwdMongoCambios pmcmsconsulta0
- databaseMongo PMCUAT01
- portMongo 27017
- hostMongo 10.100.8.80
- fileLogReportes /weblogic/pmc/logs/mspmc-reportes.log

- fileReportes /pmcarchivos/cierre_anual
- fileDictamen /pmcarchivos/dictamen
- sftp true
- sftpHost 10.100.6.98
- sftpPassword 	PMC123
- sftpPort 22
- sftpUser usr_pmc

============== AMBIENTE DE PROD ==============

Para que este microservicio pueda ser ejecutado deberán de existir las siguietnes variables de entorno en el SO annfitrión donde se ejecute el jar
- portMSCifrasCausitica 8049
- authenticationdatabaseMongo PENDIENTE
- usrMongoCambios pmcmsconsulta
- pwdMongoCambios pmcmsconsulta0
- databaseMongo PENDIENTE
- portMongo 27017
- hostMongo PENDIENTE
- fileLogReportes /weblogic/pmc/logs/mspmc-reportes.log

- fileReportes /pmcarchivos/cierre_anual
- fileDictamen /pmcarchivos/dictamen
- sftp true
- sftpHost PENDIENTE
- sftpPassword 	PENDIENTE
- sftpPort 22
- sftpUser PENDIENTE

# Getting Started
TODO: Guide users through getting your code up and running on their own system. In this section you can talk about:
1.	Installation process
2.	Software dependencies
3.	Latest releases
4.	API references

# Build and Test
TODO: Describe and show how to build your code and run the tests. 

# Contribute
TODO: Explain how other users and developers can contribute to make your code better. 

If you want to learn more about creating good readme files then refer the following [guidelines](https://docs.microsoft.com/en-us/azure/devops/repos/git/create-a-readme?view=azure-devops). You can also seek inspiration from the below readme files:
- [ASP.NET Core](https://github.com/aspnet/Home)
- [Visual Studio Code](https://github.com/Microsoft/vscode)
- [Chakra Core](https://github.com/Microsoft/ChakraCore)