deb:
	ant deb

install:
	dpkg-deb -x ../linoleum.deb debian/linoleum
