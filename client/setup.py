from setuptools import setup
from os import getenv

setup(name='exactly',
      version=getenv('EXACTLY_VERSION'),
      description='Binary exact search',
      url='http://github.com/mlinhard/exactly',
      author='Michal Linhard',
      author_email='michal@linhard.sk',
      license='Apache 2.0',
      packages=['exactly'],
      scripts=['bin/exactly'],
      data_files=[('bin', ['bin/exactly-index'])],
      zip_safe=False,
      install_requires=[
          'docopt', 'requests'
      ])
