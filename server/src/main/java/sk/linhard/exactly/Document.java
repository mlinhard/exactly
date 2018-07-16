package sk.linhard.exactly;

public interface Document<TContent> {

	int index();

	String id();

	TContent content();

}
