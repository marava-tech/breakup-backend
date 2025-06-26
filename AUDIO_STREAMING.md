# Audio Streaming API Documentation

## Overview

The Audio Streaming API provides optimized audio streaming capabilities for stories, supporting partial content requests (HTTP Range requests) for efficient playback of large audio files (up to 20+ minutes). This ensures smooth audio playback without waiting for the entire file to download.

## Features

- ✅ **HTTP Range Support**: Partial content requests for efficient streaming
- ✅ **Optimized Performance**: Chunked streaming for large audio files
- ✅ **Caching**: Audio info caching to reduce database queries
- ✅ **Multiple Formats**: Support for MP3, WAV, M4A, MP4, OGG, AAC
- ✅ **CORS Support**: Cross-origin requests for Flutter apps
- ✅ **Error Handling**: Comprehensive error handling and logging

## API Endpoints

### 1. Stream Audio
```
GET /api/audio/stream/{storyId}
```

**Headers:**
- `Range: bytes=start-end` (optional) - For partial content requests
- `Authorization: Bearer <token>` (optional) - For authenticated requests

**Response:**
- `200 OK` - Full audio content
- `206 Partial Content` - Partial audio content (when Range header is used)
- `416 Range Not Satisfiable` - Invalid range request

**Example:**
```bash
# Full audio stream
curl -X GET "http://localhost:8080/api/audio/stream/story123" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Partial content (first 1MB)
curl -X GET "http://localhost:8080/api/audio/stream/story123" \
  -H "Range: bytes=0-1048575" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Partial content (from 1MB to 2MB)
curl -X GET "http://localhost:8080/api/audio/stream/story123" \
  -H "Range: bytes=1048576-2097151" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 2. Get Audio Info
```
GET /api/audio/info/{storyId}
```

**Response:**
```json
{
  "storyId": "story123",
  "audioUrl": "https://res.cloudinary.com/dohsebpd1/audio/upload/v1750951801/story.mp3",
  "contentLength": 52428800,
  "contentType": "audio/mpeg",
  "supportsRangeRequests": true
}
```

## Flutter Integration

### Using `audioplayers` package:

```dart
import 'package:audioplayers/audioplayers.dart';

class AudioPlayerService {
  final AudioPlayer audioPlayer = AudioPlayer();
  
  Future<void> playStoryAudio(String storyId, String authToken) async {
    try {
      // Get audio info first
      final audioInfo = await getAudioInfo(storyId, authToken);
      
      // Create streaming URL
      final streamingUrl = 'http://localhost:8080/api/audio/stream/$storyId';
      
      // Play audio with streaming support
      await audioPlayer.play(UrlSource(streamingUrl),
        headers: {
          'Authorization': 'Bearer $authToken',
          'Range': 'bytes=0-', // Request from beginning
        }
      );
      
      print('Playing audio: ${audioInfo.contentLength} bytes');
      
    } catch (e) {
      print('Error playing audio: $e');
    }
  }
  
  Future<AudioInfoResponse> getAudioInfo(String storyId, String authToken) async {
    final response = await http.get(
      Uri.parse('http://localhost:8080/api/audio/info/$storyId'),
      headers: {
        'Authorization': 'Bearer $authToken',
      },
    );
    
    if (response.statusCode == 200) {
      return AudioInfoResponse.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Failed to get audio info');
    }
  }
}
```

### Using `just_audio` package (Recommended):

```dart
import 'package:just_audio/just_audio.dart';

class AudioPlayerService {
  final AudioPlayer audioPlayer = AudioPlayer();
  
  Future<void> playStoryAudio(String storyId, String authToken) async {
    try {
      // Create streaming URL with headers
      final streamingUrl = 'http://localhost:8080/api/audio/stream/$storyId';
      
      // Set up audio source with headers
      final audioSource = AudioSource.uri(
        Uri.parse(streamingUrl),
        headers: {
          'Authorization': 'Bearer $authToken',
        },
      );
      
      // Load and play
      await audioPlayer.setAudioSource(audioSource);
      await audioPlayer.play();
      
      print('Playing audio with streaming support');
      
    } catch (e) {
      print('Error playing audio: $e');
    }
  }
  
  // Control methods
  void pause() => audioPlayer.pause();
  void resume() => audioPlayer.play();
  void stop() => audioPlayer.stop();
  void seek(Duration position) => audioPlayer.seek(position);
  
  // Listen to player state
  Stream<PlayerState> get playerStateStream => audioPlayer.playerStateStream;
  Stream<Duration?> get positionStream => audioPlayer.positionStream;
  Stream<Duration?> get durationStream => audioPlayer.durationStream;
}
```

## Performance Optimizations

### 1. **Chunked Streaming**
- Audio is streamed in configurable chunks (default: 1MB)
- Reduces memory usage and improves responsiveness
- Supports seeking without downloading entire file

### 2. **Caching Strategy**
- Audio info is cached in memory to reduce database queries
- HTTP caching headers for browser/CDN caching
- Configurable cache duration (default: 1 hour)

### 3. **Range Request Support**
- Efficient partial content delivery
- Enables seeking in audio players
- Reduces bandwidth usage for large files

### 4. **Content-Type Detection**
- Automatic MIME type detection based on file extension
- Supports common audio formats: MP3, WAV, M4A, MP4, OGG, AAC

## Configuration

### Environment Variables:
```bash
# Audio streaming configuration
AUDIO_BUFFER_SIZE=8192              # Buffer size for streaming
AUDIO_CACHE_DURATION=3600           # Cache duration in seconds
AUDIO_MAX_RANGE_SIZE=1048576        # Maximum range size (1MB)
AUDIO_ENABLE_CACHING=true           # Enable/disable caching
```

### Application Properties:
```yaml
audio:
  streaming:
    buffer-size: 8192
    cache-duration: 3600
    max-range-size: 1048576
    enable-caching: true
```

## Error Handling

### Common Error Responses:

1. **Story Not Found (404)**
```json
{
  "status": 404,
  "message": "Story not found",
  "description": "Story with id 'story123' not found"
}
```

2. **Audio URL Missing (404)**
```json
{
  "status": 404,
  "message": "Audio URL not found",
  "description": "Story has no audio URL"
}
```

3. **Invalid Range Request (416)**
```http
HTTP/1.1 416 Range Not Satisfiable
Content-Range: bytes */52428800
```

4. **Audio Resource Not Found (404)**
```json
{
  "status": 404,
  "message": "Audio resource not found",
  "description": "Audio file not accessible at URL"
}
```

## Best Practices

### 1. **Flutter App Implementation**
- Use `just_audio` package for better streaming support
- Implement proper error handling and retry logic
- Add loading indicators during audio initialization
- Handle network connectivity changes

### 2. **Audio File Preparation**
- Use compressed formats (MP3, AAC) for smaller file sizes
- Ensure audio files are properly encoded for streaming
- Consider using CDN for better global performance

### 3. **Monitoring**
- Monitor audio streaming performance metrics
- Track range request patterns for optimization
- Monitor cache hit rates and memory usage

## Testing

### Test with curl:
```bash
# Test full audio stream
curl -I "http://localhost:8080/api/audio/stream/story123"

# Test range request
curl -H "Range: bytes=0-1023" "http://localhost:8080/api/audio/stream/story123"

# Test audio info
curl "http://localhost:8080/api/audio/info/story123"
```

### Test with Flutter:
```dart
// Test audio playback
final audioService = AudioPlayerService();
await audioService.playStoryAudio('story123', 'your-auth-token');

// Monitor playback state
audioService.playerStateStream.listen((state) {
  print('Player state: $state');
});
```

## Troubleshooting

### Common Issues:

1. **Audio not playing in Flutter**
   - Check network connectivity
   - Verify authentication token
   - Ensure audio URL is accessible

2. **Seeking not working**
   - Verify Range request support
   - Check audio format compatibility
   - Ensure proper headers are sent

3. **Performance issues**
   - Monitor chunk size configuration
   - Check server resources
   - Verify caching is enabled

4. **CORS errors**
   - Ensure CORS headers are properly set
   - Check origin configuration
   - Verify preflight requests

## Security Considerations

- Audio URLs are validated before streaming
- Authentication can be enforced if needed
- CORS is properly configured for Flutter apps
- Range requests are validated to prevent abuse 