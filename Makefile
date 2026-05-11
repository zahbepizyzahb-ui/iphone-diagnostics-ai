# Makefile for iPhoneDiagnosticsAI Android

.PHONY: help install backend android test clean docker

help:
	@echo "iPhone Diagnostics AI - Android"
	@echo "================================"
	@echo "  make install     - Install all dependencies"
	@echo "  make backend     - Run Flask backend"
	@echo "  make android     - Build debug APK"
	@echo "  make test        - Run all tests"
	@echo "  make clean       - Clean build files"
	@echo "  make docker      - Run with Docker"
	@echo "  make release     - Build release APK"

install:
	@echo "Installing Python dependencies..."
	cd Backend && pip install -r requirements.txt
	@echo "Done!"

backend:
	cd Backend && python app.py

docker:
	docker-compose up --build

android:
	./gradlew assembleDebug

test:
	./gradlew test
	./gradlew connectedAndroidTest

release:
	./gradlew assembleRelease

clean:
	./gradlew clean
	find . -name "*.pyc" -delete
	find . -name "__pycache__" -type d -delete
	find . -name ".gradle" -type d -exec rm -rf {} + 2>/dev/null || true
