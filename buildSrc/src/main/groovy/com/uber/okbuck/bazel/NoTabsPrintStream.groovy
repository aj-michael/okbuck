
final class NoTabsPrintStream extends PrintStream {
    private final PrintStream printStream

    NoTabsPrintStream(File outputFile) {
        super(outputFile)
    }

    @Override
    public void println(String s) {
        super.println(s.replaceAll("\t", "    "))
    }
}
