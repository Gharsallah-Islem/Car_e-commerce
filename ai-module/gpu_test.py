"""
GPU Detection and Testing Script
This script verifies that TensorFlow can detect and use your RTX 3050 GPU
"""

import sys

def check_gpu():
    """Check if GPU is available and properly configured"""
    print("=" * 60)
    print("GPU Detection and Configuration Test")
    print("=" * 60)
    
    # Check TensorFlow
    try:
        import tensorflow as tf
        print(f"\n✓ TensorFlow version: {tf.__version__}")
        
        # Check if GPU is available
        gpus = tf.config.list_physical_devices('GPU')
        
        if gpus:
            print(f"\n✓ GPU DETECTED! Found {len(gpus)} GPU(s):")
            for i, gpu in enumerate(gpus):
                print(f"  - GPU {i}: {gpu.name}")
                
            # Get GPU details
            gpu_details = tf.config.experimental.get_device_details(gpus[0])
            print(f"\nGPU Details:")
            for key, value in gpu_details.items():
                print(f"  {key}: {value}")
            
            # Test GPU memory growth (prevents TensorFlow from allocating all GPU memory)
            try:
                for gpu in gpus:
                    tf.config.experimental.set_memory_growth(gpu, True)
                print("\n✓ GPU memory growth enabled (prevents memory allocation issues)")
            except RuntimeError as e:
                print(f"\n⚠ Warning: Could not set memory growth: {e}")
            
            # Run a simple computation on GPU
            print("\n" + "=" * 60)
            print("Running GPU Test Computation...")
            print("=" * 60)
            
            with tf.device('/GPU:0'):
                # Create random matrices
                a = tf.random.normal([1000, 1000])
                b = tf.random.normal([1000, 1000])
                
                # Perform matrix multiplication
                import time
                start = time.time()
                c = tf.matmul(a, b)
                c.numpy()  # Force execution
                gpu_time = time.time() - start
                
                print(f"\n✓ GPU computation successful!")
                print(f"  Matrix multiplication (1000x1000): {gpu_time:.4f} seconds")
            
            # Compare with CPU
            with tf.device('/CPU:0'):
                start = time.time()
                c = tf.matmul(a, b)
                c.numpy()
                cpu_time = time.time() - start
                
                print(f"  CPU computation time: {cpu_time:.4f} seconds")
                print(f"  Speedup: {cpu_time/gpu_time:.2f}x faster on GPU")
            
            print("\n" + "=" * 60)
            print("✓ GPU is working correctly!")
            print("=" * 60)
            return True
            
        else:
            print("\n✗ NO GPU DETECTED!")
            print("\nPossible issues:")
            print("  1. CUDA is not installed or not in PATH")
            print("  2. cuDNN is not installed")
            print("  3. TensorFlow GPU version is not installed")
            print("  4. GPU drivers are outdated")
            print("\nTensorFlow will use CPU (training will be much slower)")
            return False
            
    except ImportError:
        print("\n✗ TensorFlow is not installed!")
        print("Run: pip install tensorflow==2.15.0")
        return False
    except Exception as e:
        print(f"\n✗ Error checking GPU: {e}")
        return False

def check_cuda():
    """Check CUDA installation"""
    print("\n" + "=" * 60)
    print("CUDA Installation Check")
    print("=" * 60)
    
    try:
        import tensorflow as tf
        
        # Check if CUDA is built with TensorFlow
        cuda_available = tf.test.is_built_with_cuda()
        print(f"\nTensorFlow built with CUDA: {cuda_available}")
        
        if cuda_available:
            # Try to get CUDA version
            try:
                import subprocess
                result = subprocess.run(['nvcc', '--version'], 
                                      capture_output=True, 
                                      text=True)
                if result.returncode == 0:
                    print(f"\nCUDA Compiler (nvcc) output:")
                    print(result.stdout)
            except:
                print("\nCould not run nvcc command")
        
    except Exception as e:
        print(f"Error checking CUDA: {e}")

def main():
    """Main function"""
    print("\n")
    check_cuda()
    print("\n")
    gpu_available = check_gpu()
    
    print("\n" + "=" * 60)
    print("Summary")
    print("=" * 60)
    
    if gpu_available:
        print("\n✓ Your RTX 3050 is properly configured!")
        print("✓ TensorFlow will use GPU for training")
        print("✓ You're ready to train the visual search model")
    else:
        print("\n✗ GPU is not available")
        print("⚠ Training will be MUCH slower on CPU")
        print("\nTo fix:")
        print("1. Make sure CUDA 12.2 is installed")
        print("2. Make sure cuDNN is installed")
        print("3. Reinstall TensorFlow: pip install tensorflow==2.15.0")
        print("4. Restart your terminal/IDE")
    
    print("\n")

if __name__ == "__main__":
    main()
