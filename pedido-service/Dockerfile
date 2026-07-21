FROM golang:1.26.5-alpine AS build
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 go build -o pedidos .

FROM alpine:3.21
WORKDIR /app
COPY --from=build /app/pedidos .
ENTRYPOINT ["./pedidos"]