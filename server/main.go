package main

import (
	"flag"
	"fmt"
	"os"
	"os/signal"
	"syscall"

	"github.com/dustin/go-humanize"
	"github.com/mlinhard/exactly/server/server"
)

var Version string

func main() {
	configFile := flag.String("config", "-", "Configuration file")
	flag.Parse()
	var config *server.ServerConfig
	var err error
	if configFile != nil && *configFile != "-" {
		config, err = server.LoadConfigFrom(*configFile)
	} else {
		config, err = server.LoadConfig()
	}
	if err != nil {
		fmt.Printf("ERROR: %v\n", err)
		return
	}
	server := server.NewServer(Version, config)

	server.OnDoneCrawling(func(size int, totalBytes int) {
		fmt.Printf("Found %v files, %v. Loading ...\n", size, humanize.Bytes(uint64(totalBytes)))
	})
	server.OnDoneLoading(func(size int, totalBytes int) {
		fmt.Printf("Indexing ...\n")
	})
	server.OnDoneIndexing(func(size int, totalBytes int) {
		fmt.Printf("Ready to search.\n")
	})
	server.OnError(func(err error) {
		fmt.Printf("ERROR: %v\n", err)
	})

	done := make(chan os.Signal, 1)
	signal.Notify(done, os.Interrupt, syscall.SIGINT, syscall.SIGTERM)
	server.Start()
	<-done
	server.Stop()
	fmt.Printf("Server terminated.\n")
}
