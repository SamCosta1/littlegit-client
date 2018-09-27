package org.littlegit.client.env

enum class Environment {
    Local,
    Dev;

    val baseUrl: String; get() = when(this) {
        Environment.Local -> "localhost:8080"
        Environment.Dev -> "http://ec2-34-253-241-12.eu-west-1.compute.amazonaws.com:8080"
    }
}