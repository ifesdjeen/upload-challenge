#
# SSH
#


default_run_options[:pty] = true
ssh_options[:forward_agent] = true

#
# Stages
#

set :stages, %w(production)
set :default_stage, "production"
require 'capistrano/ext/multistage'

#
# Vars
#

set :application, "uploadchallenge"
set :repository,  "git://github.com/ifesdjeen/upload-challenge.git"
set :branch,      "master"
set :scm,         :git
set :copy_strategy, :export

set :deploy_via, :copy

set :build_script, "lein2 with-profile deployment deps && lein2 with-profile deployment javac && lein2 with-profile deployment uberjar"

set :build_artifact, "target/uploadchallenge-0.1.0-SNAPSHOT-standalone.jar"

set :use_sudo,   false
set :deploy_to,  "/applications/#{application}"


namespace :deploy do
  desc "Start Upload"
  task :start, :roles => :app do
    run "#{current_release}/initscript.sh start"
  end

  task :stop, :roles => :app do
    run "#{current_release}/initscript.sh stop"
  end

  task :restart, :roles => :app do
    deploy.stop
    deploy.start
  end

  task :finalize_update do
  end
  task :migrate do
  end

  task :update_symlinks do
    run "ln -s #{shared_path}/log #{current_release}/log"
    run "mkdir #{current_release}/tmp"
    run "ln -s #{shared_path}/pids #{current_release}/tmp/pids"
  end
end

after "deploy:create_symlink", "deploy:update_symlinks"


