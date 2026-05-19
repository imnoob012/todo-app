package main

import (
	"log"
	"net/http"
	"os"
	"time"

	"github.com/imnoob012/todo-app/backend/internal/httpapi"
	"github.com/imnoob012/todo-app/backend/internal/store"
)

func main() {
	port := env("PORT", "8080")
	staticDir := env("STATIC_DIR", "../frontend/dist")

	appStore := store.NewMemoryStore()
	server := httpapi.NewServer(appStore, staticDir)

	httpServer := &http.Server{
		Addr:              ":" + port,
		Handler:           server,
		ReadHeaderTimeout: 5 * time.Second,
	}

	log.Printf("todo-app Go API listening on http://localhost:%s", port)
	if err := httpServer.ListenAndServe(); err != nil && err != http.ErrServerClosed {
		log.Fatal(err)
	}
}

func env(key, fallback string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return fallback
}
