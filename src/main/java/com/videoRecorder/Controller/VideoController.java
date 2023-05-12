package com.videoRecorder.Controller;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@Controller
@RequestMapping("/")
public class VideoController {
    private FrameGrabber grabber;
    private FrameRecorder recorder;
    private boolean recording;

    @GetMapping("")
    public String getHome(Model model) {
        model.addAttribute("pageName", "Video Recorder");
        model.addAttribute("recording", recording);
        return "index";
    }

    @PostMapping("/updateRecording")
    public ResponseEntity<String> updateRecording(@RequestParam("recording") Boolean toggleRecordingStart) {
        if (toggleRecordingStart) {
            try {
                if (recording) {
                    throw new IllegalStateException("Recording is already in progress.");
                }
                grabber = FrameGrabber.createDefault(0);
                grabber.start();
                recorder = FrameRecorder.createDefault("output" + System.currentTimeMillis() + ".mp4", grabber.getImageWidth(), grabber.getImageHeight());
                recorder.start();
                recording = true;
                Thread recordingThread = new Thread(this::recordFrames);
                recordingThread.start();
                return new ResponseEntity<>("Recording started successfully", HttpStatus.OK);
            } catch (IllegalStateException | FrameGrabber.Exception | FrameRecorder.Exception e) {
                return new ResponseEntity<>("Recording start error " + e, HttpStatus.I_AM_A_TEAPOT);
            }
        } else {
            try {
                if (!recording) {
                    throw new IllegalStateException("No recording in progress.");
                }
                recording = false;

                grabber.stop();
                grabber.release();
                recorder.stop();
                recorder.release();
                return new ResponseEntity<>("Recording stopped successfully", HttpStatus.OK);
            } catch (IllegalStateException | FrameGrabber.Exception | FrameRecorder.Exception e) {
                return new ResponseEntity<>("Recording stopped error " + e, HttpStatus.I_AM_A_TEAPOT);
            }
        }
    }

    private void recordFrames() {
        try {
            while (recording) {
                Frame frame = grabber.grab();
                BufferedImage bufferedImage = new Java2DFrameConverter().convert(frame);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpg", baos);
                byte[] frameBytes = baos.toByteArray();
                recorder.record(frame);
                Thread.sleep(33);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}




