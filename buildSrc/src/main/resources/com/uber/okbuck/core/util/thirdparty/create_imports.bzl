def create_imports():
  for jar in native.glob(['cache/*.jar']):
    native.java_import(
        name = jar.split("/")[1],
        jars = [jar],
    )
  for aar in native.glob(['cache/*.aar']):
    native.aar_import(
        name = aar.split("/")[1],
        aar = aar,
    )